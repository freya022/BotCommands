package io.github.freya022.botcommands.internal.commands.application

import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandRejectionHandler
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.checkFilters
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.getMissingPermissions
import io.github.freya022.botcommands.internal.commands.Usability
import io.github.freya022.botcommands.internal.commands.Usability.UnusableReason
import io.github.freya022.botcommands.internal.commands.application.slash.SlashCommandInfo
import io.github.freya022.botcommands.internal.commands.ratelimit.withRateLimit
import io.github.freya022.botcommands.internal.core.ExceptionHandler
import io.github.freya022.botcommands.internal.utils.classRef
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent

@BService
internal class ApplicationCommandListener internal constructor(
    private val context: BContext,
    filters: List<ApplicationCommandFilter<Any>>,
    rejectionHandler: ApplicationCommandRejectionHandler<Any>?
) {
    private val logger = KotlinLogging.logger {  }
    private val exceptionHandler = ExceptionHandler(context, logger)

    // Types are crosschecked anyway
    private val globalFilters: List<ApplicationCommandFilter<Any>> = filters.filter { it.global }
    private val rejectionHandler: ApplicationCommandRejectionHandler<Any>? = when {
        filters.isEmpty() -> null
        else -> rejectionHandler
            ?: throw IllegalStateException("A ${classRef<ApplicationCommandRejectionHandler<*>>()} must be available if ${classRef<ApplicationCommandFilter<*>>()} is used")
    }

    @BEventListener
    suspend fun onSlashCommand(event: SlashCommandInteractionEvent) {
        logger.trace { "Received slash command: ${reconstructCommand(event)}" }

        context.coroutineScopesConfig.applicationCommandsScope.launch {
            try {
                // In rare cases where a user sends a command before they have been registered
                if (context.applicationCommandsContext.getLiveApplicationCommandsMap(event.guild) == null) {
                    return@launch event.reply_(context.getDefaultMessages(event).applicationCommandsNotAvailableMsg, ephemeral = true).queue()
                }

                val slashCommand = CommandPath.of(event.name, event.subcommandGroup, event.subcommandName).let {
                    context.applicationCommandsContext.findLiveSlashCommand(event.guild, it)
                        ?: return@launch onCommandNotFound(event, "A slash command could not be found: ${event.fullCommandName}")
                }

                val isNotOwner = !context.isOwner(event.user.idLong)
                slashCommand.withRateLimit(context, event, isNotOwner) { cancellableRateLimit ->
                    if (!canRun(event, slashCommand, isNotOwner)) {
                        false
                    } else {
                        slashCommand.execute(event, cancellableRateLimit)
                    }
                }
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
                // In rare cases where a user sends a command before they have been registered
                if (context.applicationCommandsContext.getLiveApplicationCommandsMap(event.guild) == null) {
                    return@launch event.reply_(context.getDefaultMessages(event).applicationCommandsNotAvailableMsg, ephemeral = true).queue()
                }

                val userCommand = event.name.let {
                    context.applicationCommandsContext.findLiveUserCommand(event.guild, it)
                        ?: return@launch onCommandNotFound(event, "A user context command could not be found: ${event.name}")
                }

                val isNotOwner = !context.isOwner(event.user.idLong)
                userCommand.withRateLimit(context, event, isNotOwner) { cancellableRateLimit ->
                    if (!canRun(event, userCommand, isNotOwner)) {
                        false
                    } else {
                        userCommand.execute(event, cancellableRateLimit)
                    }
                }
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
                // In rare cases where a user sends a command before they have been registered
                if (context.applicationCommandsContext.getLiveApplicationCommandsMap(event.guild) == null) {
                    return@launch event.reply_(context.getDefaultMessages(event).applicationCommandsNotAvailableMsg, ephemeral = true).queue()
                }

                val messageCommand = event.name.let {
                    context.applicationCommandsContext.findLiveMessageCommand(event.guild, it)
                        ?: return@launch onCommandNotFound(event, "A message context command could not be found: ${event.name}")
                }

                val isNotOwner = !context.isOwner(event.user.idLong)
                messageCommand.withRateLimit(context, event, isNotOwner) { cancellableRateLimit ->
                    if (!canRun(event, messageCommand, isNotOwner)) {
                        false
                    } else {
                        messageCommand.execute(event, cancellableRateLimit)
                    }
                }
            } catch (e: Throwable) {
                handleException(e, event)
            }
        }
    }

    private fun onCommandNotFound(event: GenericCommandInteractionEvent, message: String) {
        // If an exception occurred during global commands registration, the command map for it does not exist
        if (context.applicationCommandsContext.getLiveApplicationCommandsMap(null) == null) {
            return event.reply_(context.getDefaultMessages(event).applicationCommandsNotAvailableMsg, ephemeral = true).queue()
        }

        //This is done so warnings are printed after the exception
        handleException(IllegalArgumentException(message), event)
        printAvailableCommands(event)
    }

    private fun printAvailableCommands(event: GenericCommandInteractionEvent) {
        val guild = event.guild
        logger.debug {
            val commandsMap = context.applicationCommandsContext.getEffectiveApplicationCommandsMap(guild)
            val scopeName = if (guild != null) "'" + guild.name + "'" else "Global scope"
            val availableCommands = commandsMap.allApplicationCommands
                .map { commandInfo ->
                    when (commandInfo) {
                        is SlashCommandInfo -> "/" + commandInfo.path.getFullPath(' ')
                        else -> commandInfo.path.fullPath
                    }
                }
                .sorted()
                .joinToString("\n")
            "Commands available in $scopeName:\n$availableCommands"
        }

        if (context.applicationConfig.onlineAppCommandCheckEnabled) {
            logger.warn {
                """
                    An application command could not be recognized even though online command check was performed. An update will be forced.
                    Please check if you have another bot instance running as it could have replaced the current command set.
                    Do not share your tokens with anyone else (even your friend), and use a separate token when testing.
                """.trimIndent()
            }
            if (guild != null) {
                context.applicationCommandsContext.updateGuildApplicationCommands(guild, force = true).whenComplete { _, e ->
                    if (e != null)
                        logger.error(e) { "An exception occurred while trying to update commands of guild '${guild.name}' (${guild.id}) after a command was missing" }
                }
            } else {
                context.applicationCommandsContext.updateGlobalApplicationCommands(force = true).whenComplete { _, e ->
                    if (e != null)
                        logger.error(e) { "An exception occurred while trying to update global commands after a command was missing" }
                }
            }
        }
    }

    private fun handleException(e: Throwable, event: GenericCommandInteractionEvent) {
        exceptionHandler.handleException(event, e, "application command '${reconstructCommand(event)}'", emptyMap())

        val generalErrorMsg = context.getDefaultMessages(event).generalErrorMsg
        when {
            event.isAcknowledged -> event.hook.sendMessage(generalErrorMsg).setEphemeral(true).queue()
            else -> event.reply(generalErrorMsg).setEphemeral(true).queue()
        }
    }

    private suspend fun canRun(
        event: GenericCommandInteractionEvent,
        applicationCommand: ApplicationCommandInfo,
        isNotOwner: Boolean
    ): Boolean {
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
                    val member = event.member ?: throwInternal("USER_PERMISSIONS got checked even if guild is null")
                    val missingPermissions = getMissingPermissions(applicationCommand.userPermissions, member, event.guildChannel)
                    reply(event, context.getDefaultMessages(event).getUserPermErrorMsg(missingPermissions))
                    return false
                }
                UnusableReason.BOT_PERMISSIONS in unusableReasons -> {
                    val guild = event.guild ?: throwInternal("BOT_PERMISSIONS got checked even if guild is null")
                    val missingPermissions = getMissingPermissions(applicationCommand.botPermissions, guild.selfMember, event.guildChannel)
                    reply(event, context.getDefaultMessages(event).getBotPermErrorMsg(missingPermissions))
                    return false
                }
            }
        }

        checkFilters(globalFilters, applicationCommand.filters) { filter ->
            val userError = filter.checkSuspend(event, applicationCommand)
            if (userError != null) {
                rejectionHandler!!.handleSuspend(event, applicationCommand, userError)
                if (event.isAcknowledged) {
                    logger.trace { "${filter.description} rejected application command '${event.commandString}'" }
                } else {
                    logger.error { "${filter.description} rejected application command '${event.commandString}' but did not acknowledge the interaction" }
                }
                return false
            }
        }

        return true
    }

    private fun reply(event: GenericCommandInteractionEvent, msg: String) {
        event.reply_(msg, ephemeral = true)
            .queue(null) { throwable ->
                exceptionHandler.handleException(event, throwable, "interaction reply", emptyMap())
            }
    }

    private fun reconstructCommand(event: GenericCommandInteractionEvent): String {
        return when (event) {
            is SlashCommandInteractionEvent -> event.commandString
            else -> event.name
        }
    }
}