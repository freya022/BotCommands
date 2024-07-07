package io.github.freya022.botcommands.internal.commands.application

import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandRejectionHandler
import io.github.freya022.botcommands.api.commands.application.context.message.GlobalMessageEvent
import io.github.freya022.botcommands.api.commands.application.context.message.GuildMessageEvent
import io.github.freya022.botcommands.api.commands.application.context.user.GlobalUserEvent
import io.github.freya022.botcommands.api.commands.application.context.user.GuildUserEvent
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.checkFilters
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.getMissingPermissions
import io.github.freya022.botcommands.api.localization.DefaultMessagesFactory
import io.github.freya022.botcommands.internal.commands.Usability
import io.github.freya022.botcommands.internal.commands.Usability.UnusableReason
import io.github.freya022.botcommands.internal.commands.application.context.message.MessageCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.application.context.user.UserCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.application.slash.SlashCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.ratelimit.withRateLimit
import io.github.freya022.botcommands.internal.core.ExceptionHandler
import io.github.freya022.botcommands.internal.localization.interaction.LocalizableInteractionFactory
import io.github.freya022.botcommands.internal.utils.*
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException

private val logger = KotlinLogging.logger {  }

@BService
internal class ApplicationCommandListener internal constructor(
    private val context: BContext,
    private val defaultMessagesFactory: DefaultMessagesFactory,
    private val localizableInteractionFactory: LocalizableInteractionFactory,
    filters: List<ApplicationCommandFilter<Any>>,
    rejectionHandler: ApplicationCommandRejectionHandler<Any>?
) {
    private val scope = context.coroutineScopesConfig.applicationCommandsScope
    private val exceptionHandler = ExceptionHandler(context, logger)

    // Types are crosschecked anyway
    private val globalFilters: List<ApplicationCommandFilter<Any>> = filters.filter { it.global }
    private val rejectionHandler: ApplicationCommandRejectionHandler<Any>? = when {
        filters.isEmpty() -> null
        else -> rejectionHandler
            ?: throwState("A ${classRef<ApplicationCommandRejectionHandler<*>>()} must be available if ${classRef<ApplicationCommandFilter<*>>()} is used")
    }

    @BEventListener
    suspend fun onSlashCommand(event: SlashCommandInteractionEvent) {
        logger.trace { "Received slash command: ${event.commandString}" }

        scope.launchCatching({ handleException(it, event) }) launch@{
            val slashCommand = CommandPath.of(event.name, event.subcommandGroup, event.subcommandName).let {
                context.applicationCommandsContext.findLiveSlashCommand(event.guild, it)
                    ?: return@launch onCommandNotFound(event, "A slash command could not be found: ${event.fullCommandName}")
            } as SlashCommandInfoImpl

            val isNotOwner = !context.isOwner(event.user.idLong)
            slashCommand.withRateLimit(context, event, isNotOwner) { cancellableRateLimit ->
                if (!canRun(event, slashCommand, isNotOwner)) {
                    false
                } else {
                    val localizableInteraction = localizableInteractionFactory.create(event)
                    val bcEvent = when {
                        slashCommand.topLevelInstance.isGuildOnly -> GuildSlashEvent(context, event, cancellableRateLimit, localizableInteraction)
                        else -> GlobalSlashEvent(context, event, cancellableRateLimit, localizableInteraction)
                    }
                    slashCommand.execute(bcEvent)
                }
            }
        }
    }

    @BEventListener
    suspend fun onUserContextCommand(event: UserContextInteractionEvent) {
        logger.trace { "Received user context command: ${event.name}" }

        scope.launchCatching({ handleException(it, event) }) launch@{
            val userCommand = event.name.let {
                context.applicationCommandsContext.findLiveUserCommand(event.guild, it)
                    ?: return@launch onCommandNotFound(event, "A user context command could not be found: ${event.name}")
            } as UserCommandInfoImpl

            val isNotOwner = !context.isOwner(event.user.idLong)
            userCommand.withRateLimit(context, event, isNotOwner) { cancellableRateLimit ->
                if (!canRun(event, userCommand, isNotOwner)) {
                    false
                } else {
                    val localizableInteraction = localizableInteractionFactory.create(event)
                    val bcEvent = when {
                        userCommand.isGuildOnly -> GuildUserEvent(context, event, cancellableRateLimit, localizableInteraction)
                        else -> GlobalUserEvent(context, event, cancellableRateLimit, localizableInteraction)
                    }
                    userCommand.execute(bcEvent)
                }
            }
        }
    }

    @BEventListener
    suspend fun onMessageContextCommand(event: MessageContextInteractionEvent) {
        logger.trace { "Received message context command: ${event.name}" }

        scope.launchCatching({ handleException(it, event) }) launch@{
            val messageCommand = event.name.let {
                context.applicationCommandsContext.findLiveMessageCommand(event.guild, it)
                    ?: return@launch onCommandNotFound(event, "A message context command could not be found: ${event.name}")
            } as MessageCommandInfoImpl

            val isNotOwner = !context.isOwner(event.user.idLong)
            messageCommand.withRateLimit(context, event, isNotOwner) { cancellableRateLimit ->
                if (!canRun(event, messageCommand, isNotOwner)) {
                    false
                } else {
                    val localizableInteraction = localizableInteractionFactory.create(event)
                    val bcEvent = when {
                        messageCommand.isGuildOnly -> GuildMessageEvent(context, event, cancellableRateLimit, localizableInteraction)
                        else -> GlobalMessageEvent(context, event, cancellableRateLimit, localizableInteraction)
                    }
                    messageCommand.execute(bcEvent)
                }
            }
        }
    }

    // In rare cases where a user sends a command before they have been registered
    // Or the command list is somehow not the same
    private suspend fun onCommandNotFound(event: GenericCommandInteractionEvent, message: String) {
        // If an exception occurred during first-time commands registration, the command map for it does not exist
        val guildMap = context.applicationCommandsContext.getLiveApplicationCommandsMap(event.guild)
        val globalMap = context.applicationCommandsContext.getLiveApplicationCommandsMap(null)
        if (guildMap == null || globalMap == null) {
            return event.reply_(defaultMessagesFactory.get(event).applicationCommandsNotAvailableMsg, ephemeral = true).queue()
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
                        is SlashCommandInfoImpl -> "/" + commandInfo.path.getFullPath(' ')
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

    private suspend fun handleException(e: Throwable, event: GenericCommandInteractionEvent) {
        exceptionHandler.handleException(event, e, "application command '${event.commandString}'", emptyMap())

        if (e is InsufficientPermissionException) {
            event.replyExceptionMessage(defaultMessagesFactory.get(event).getBotPermErrorMsg(setOf(e.permission)))
        } else {
            event.replyExceptionMessage(defaultMessagesFactory.get(event).generalErrorMsg)
        }
    }

    private suspend fun canRun(
        event: GenericCommandInteractionEvent,
        applicationCommand: ApplicationCommandInfoImpl,
        isNotOwner: Boolean
    ): Boolean {
        val usability = Usability.of(event, applicationCommand, isNotOwner)
        if (usability.isUnusable) {
            val unusableReasons = usability.unusableReasons
            when {
                UnusableReason.OWNER_ONLY in unusableReasons -> {
                    reply(event, defaultMessagesFactory.get(event).ownerOnlyErrorMsg)
                    return false
                }
                UnusableReason.NSFW_ONLY in unusableReasons -> throwInternal("Discord already handles NSFW commands")
                UnusableReason.USER_PERMISSIONS in unusableReasons -> {
                    val member = event.member ?: throwInternal("USER_PERMISSIONS got checked even if guild is null")
                    val missingPermissions = getMissingPermissions(applicationCommand.userPermissions, member, event.guildChannel)
                    reply(event, defaultMessagesFactory.get(event).getUserPermErrorMsg(missingPermissions))
                    return false
                }
                UnusableReason.BOT_PERMISSIONS in unusableReasons -> {
                    val guild = event.guild ?: throwInternal("BOT_PERMISSIONS got checked even if guild is null")
                    val missingPermissions = getMissingPermissions(applicationCommand.botPermissions, guild.selfMember, event.guildChannel)
                    reply(event, defaultMessagesFactory.get(event).getBotPermErrorMsg(missingPermissions))
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
}