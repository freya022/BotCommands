package com.freya02.botcommands.internal.commands.application

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.CooldownScope
import com.freya02.botcommands.api.commands.application.ApplicationFilteringData
import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.Usability.UnusableReason
import com.freya02.botcommands.internal.core.CooldownService
import dev.minn.jda.ktx.messages.reply_
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import java.util.*

@BService
internal class ApplicationCommandListener(private val context: BContextImpl, private val cooldownService: CooldownService) {
    private val logger = KotlinLogging.logger {  }
    private val exceptionHandler = ExceptionHandler(context, logger)

    @BEventListener
    suspend fun onSlashCommand(event: SlashCommandInteractionEvent) {
        logger.trace { "Received slash command: ${reconstructCommand(event)}" }

        context.coroutineScopesConfig.applicationCommandsScope.launch {
            try {
                val slashCommand = CommandPath.of(event.fullCommandName).let {
                    context.applicationCommandsContext.findLiveSlashCommand(event.guild, it)
                        ?: context.applicationCommandsContext.findLiveSlashCommand(null, it)
                        ?: throwUser("A slash command could not be found: ${event.fullCommandName}")
                }

                if (!canRun(event, slashCommand)) return@launch
                slashCommand.execute(event, cooldownService)
            } catch (e: Throwable) {
                handleException(e, event)
            }
        }
    }

    @BEventListener
    suspend fun onUserContextCommand(event: UserContextInteractionEvent) {
        logger.trace { "Received user context command: ${reconstructCommand(event)}" }

        context.coroutineScopesConfig.applicationCommandsScope.launch {
            try {
                val userCommand = event.name.let {
                    context.applicationCommandsContext.findLiveUserCommand(event.guild, it)
                        ?: context.applicationCommandsContext.findLiveUserCommand(null, it)
                        ?: throwUser("A user context command could not be found: ${event.name}")
                }

                if (!canRun(event, userCommand)) return@launch
                userCommand.execute(event, cooldownService)
            } catch (e: Throwable) {
                handleException(e, event)
            }
        }
    }

    @BEventListener
    suspend fun onMessageContextCommand(event: MessageContextInteractionEvent) {
        logger.trace { "Received message context command: ${reconstructCommand(event)}" }

        context.coroutineScopesConfig.applicationCommandsScope.launch {
            try {
                val messageCommand = event.name.let {
                    context.applicationCommandsContext.findLiveMessageCommand(event.guild, it)
                        ?: context.applicationCommandsContext.findLiveMessageCommand(null, it)
                        ?: throwUser("A message context command could not be found: ${event.name}")
                }

                if (!canRun(event, messageCommand)) return@launch
                messageCommand.execute(event, cooldownService)
            } catch (e: Throwable) {
                handleException(e, event)
            }
        }
    }

    private fun handleException(e: Throwable, event: GenericCommandInteractionEvent) {
        exceptionHandler.handleException(event, e, "application command '${reconstructCommand(event)}'")

        val generalErrorMsg = context.getDefaultMessages(event).generalErrorMsg
        when {
            event.isAcknowledged -> event.hook.sendMessage(generalErrorMsg).setEphemeral(true).queue()
            else -> event.reply(generalErrorMsg).setEphemeral(true).queue()
        }
    }

    private fun canRun(event: GenericCommandInteractionEvent, applicationCommand: ApplicationCommandInfo): Boolean {
        val applicationFilteringData = ApplicationFilteringData(context, event, applicationCommand)
        for (applicationFilter in context.applicationConfig.applicationFilters) {
            if (!applicationFilter.isAccepted(applicationFilteringData)) {
                logger.trace("Cancelled application commands due to filter")
                return false
            }
        }

        val isNotOwner = !context.isOwner(event.user.idLong)
        val usability = Usability.of(event, applicationCommand, isNotOwner)
        if (usability.isUnusable) {
            val unusableReasons = usability.unusableReasons
            when {
                UnusableReason.OWNER_ONLY in unusableReasons -> {
                    reply(event, context.getDefaultMessages(event).ownerOnlyErrorMsg)
                    return false
                }
                UnusableReason.NSFW_DISABLED in unusableReasons -> {
                    reply(event, context.getDefaultMessages(event).nsfwDisabledErrorMsg)
                    return false
                }
                UnusableReason.NSFW_ONLY in unusableReasons -> {
                    reply(event, context.getDefaultMessages(event).nsfwOnlyErrorMsg)
                    return false
                }
                UnusableReason.NSFW_DM_DENIED in unusableReasons -> {
                    reply(event, context.getDefaultMessages(event).nsfwdmDeniedErrorMsg)
                    return false
                }
                UnusableReason.USER_PERMISSIONS in unusableReasons -> {
                    reply(event, context.getDefaultMessages(event).userPermErrorMsg)
                    return false
                }
                UnusableReason.BOT_PERMISSIONS in unusableReasons -> {
                    if (event.guild == null) throwInternal("BOT_PERMISSIONS got checked even if guild is null")
                    val missingBuilder = StringJoiner(", ")

                    //Take needed permissions, extract bot current permissions
                    val missingPerms = applicationCommand.botPermissions
                    missingPerms.removeAll(event.guild!!.selfMember.getPermissions(event.guildChannel))
                    for (botPermission in missingPerms) {
                        missingBuilder.add(botPermission.getName())
                    }
                    reply(event, context.getDefaultMessages(event).getBotPermErrorMsg(missingBuilder.toString()))

                    return false
                }
            }
        }

        if (isNotOwner) {
            val cooldown = cooldownService.getCooldown(applicationCommand, event)
            if (cooldown > 0) {
                val messages = context.getDefaultMessages(event)

                when (applicationCommand.cooldownStrategy.scope) {
                    CooldownScope.USER -> reply(event, messages.getUserCooldownMsg(cooldown / 1000.0))
                    CooldownScope.GUILD -> reply(event, messages.getGuildCooldownMsg(cooldown / 1000.0))
                    //Implicit CooldownScope.CHANNEL
                    else -> reply(event, messages.getChannelCooldownMsg(cooldown / 1000.0))
                }

                return false
            }
        }

        return true
    }

    private fun reply(event: GenericCommandInteractionEvent, msg: String) {
        event.reply_(msg, ephemeral = true)
            .queue(null) { exceptionHandler.handleException(event, it, "interaction reply") }
    }

    private fun reconstructCommand(event: GenericCommandInteractionEvent): String {
        return when (event) {
            is SlashCommandInteractionEvent -> event.commandString
            else -> event.name
        }
    }
}