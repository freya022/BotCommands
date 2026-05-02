package io.github.freya022.botcommands.internal.commands.application

import io.github.freya022.botcommands.api.commands.Usability.UnusableReason
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandsContext
import io.github.freya022.botcommands.api.commands.application.annotations.RequiresApplicationCommands
import io.github.freya022.botcommands.api.commands.application.context.message.GlobalMessageEvent
import io.github.freya022.botcommands.api.commands.application.context.message.GuildMessageEvent
import io.github.freya022.botcommands.api.commands.application.context.user.GlobalUserEvent
import io.github.freya022.botcommands.api.commands.application.context.user.GuildUserEvent
import io.github.freya022.botcommands.api.commands.application.getApplicationCommandById
import io.github.freya022.botcommands.api.commands.application.messages.ApplicationCommandsMessages
import io.github.freya022.botcommands.api.commands.application.messages.ApplicationCommandsMessagesFactory
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.TopLevelSlashCommandInfo
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.checkFilters
import io.github.freya022.botcommands.api.core.entities.inputUser
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.getMissingPermissions
import io.github.freya022.botcommands.internal.commands.application.cache.factory.ApplicationCommandsCacheFactory
import io.github.freya022.botcommands.internal.commands.application.context.message.MessageCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.application.context.user.UserCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.application.ratelimit.ApplicationCommandRateLimitHandler
import io.github.freya022.botcommands.internal.commands.application.slash.SlashCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.application.slash.exceptions.OptionNotFoundException
import io.github.freya022.botcommands.internal.core.ExceptionHandler
import io.github.freya022.botcommands.internal.core.exceptions.getDiagnosticVersions
import io.github.freya022.botcommands.internal.localization.interaction.LocalizableInteractionFactory
import io.github.freya022.botcommands.internal.utils.replyExceptionMessage
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.Level
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.interactions.Interaction
import net.dv8tion.jda.api.utils.messages.MessageCreateData

private val logger = KotlinLogging.logger {  }

@BService
@RequiresApplicationCommands
internal class ApplicationCommandListener internal constructor(
    private val context: BContext,
    private val applicationCommandsContext: ApplicationCommandsContext,
    private val applicationCommandsBuilder: ApplicationCommandsBuilder,
    private val messagesFactory: ApplicationCommandsMessagesFactory,
    private val localizableInteractionFactory: LocalizableInteractionFactory,
    private val rateLimitHandler: ApplicationCommandRateLimitHandler,
    filters: List<ApplicationCommandFilter>,
) {
    private val scope = context.coroutineScopesConfig.applicationCommandsScope
    private val exceptionHandler = ExceptionHandler(context, logger)

    private val globalFilters = filters.filter { it.global }

    @BEventListener
    fun onSlashCommand(event: SlashCommandInteractionEvent) {
        logger.trace { "Received slash command: ${event.commandString}" }

        scope.launch {
            try {
                handleSlashCommand(event)
            } catch (e: Exception) {
                handleException(e, event, CommandType.SLASH, emptyMap())
            }
        }
    }

    private suspend fun handleSlashCommand(event: SlashCommandInteractionEvent) {
        val slashCommand = applicationCommandsContext
            .getApplicationCommandById<SlashCommandInfoImpl>(event.commandIdLong, event.subcommandGroup, event.subcommandName)
            ?: return onCommandNotFound(event, "A slash command could not be found: ${event.fullCommandName}")

        rateLimitHandler.tryRun(slashCommand, event) { cancellableRateLimit ->
            if (!canRun(event, slashCommand)) {
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

    @BEventListener
    fun onUserContextCommand(event: UserContextInteractionEvent) {
        logger.trace { "Received user context command: ${event.name}" }

        scope.launch {
            try {
                handleUserContextCommand(event)
            } catch (e: Exception) {
                handleException(e, event, CommandType.USER_CONTEXT, mapOf("User" to event.target.asMention))
            }
        }
    }

    private suspend fun handleUserContextCommand(event: UserContextInteractionEvent) {
        val userCommand = applicationCommandsContext
            .getApplicationCommandById<UserCommandInfoImpl>(event.commandIdLong, group = null, subcommand = null)
            ?: return onCommandNotFound(event, "A user context command could not be found: ${event.name}")

        rateLimitHandler.tryRun(userCommand, event) { cancellableRateLimit ->
            if (!canRun(event, userCommand)) {
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

    @BEventListener
    fun onMessageContextCommand(event: MessageContextInteractionEvent) {
        logger.trace { "Received message context command: ${event.name}" }

        scope.launch {
            try {
                handleMessageContextCommand(event)
            } catch (e: Exception) {
                handleException(e, event, CommandType.MESSAGE_CONTEXT, mapOf("Message" to event.target.jumpUrl))
            }
        }
    }

    private suspend fun handleMessageContextCommand(event: MessageContextInteractionEvent) {
        val messageCommand = applicationCommandsContext
            .getApplicationCommandById<MessageCommandInfoImpl>(event.commandIdLong, group = null, subcommand = null)
            ?: return onCommandNotFound(event, "A message context command could not be found: ${event.name}")

        rateLimitHandler.tryRun(messageCommand, event) { cancellableRateLimit ->
            if (!canRun(event, messageCommand)) {
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

    // In rare cases where a user sends a command before they have been registered
    // Or the command list is somehow different
    private suspend fun onCommandNotFound(event: GenericCommandInteractionEvent, message: String) {
        val guild = event.guild
        val failedGlobal = !applicationCommandsBuilder.hasPushedGlobalOnceSuccessfully()
        val failedGuild = if (guild != null) !applicationCommandsBuilder.hasPushedGuildOnceSuccessfully(guild) else false
        if (failedGlobal || failedGuild) {
            if (failedGlobal && failedGuild) {
                logger.debug { "Ignored '${event.fullCommandName}' as global command and guild commands (${guild!!.id}) could not be updated" }
            } else if (failedGlobal) {
                logger.debug { "Ignored '${event.fullCommandName}' as global commands could not be updated" }
            } else {
                logger.debug { "Ignored '${event.fullCommandName}' as guild (${guild!!.id}) commands could not be updated" }
            }
            return event.reply(messagesFactory.get(event).applicationCommandsNotAvailable(event)).setEphemeral(true).queue()
        }

        //This is done so warnings are printed after the exception
        handleException(IllegalArgumentException(message), event, CommandType.APPLICATION, emptyMap())
        printAvailableCommands(event)
        logger.warn {
            if (context.getService<ApplicationCommandsCacheFactory>().cacheConfig.checkOnline) {
                createCommandMismatchMessage("An application command could not be recognized even though online command check was performed, an update will be forced.")
            } else {
                createCommandMismatchMessage("An application command could not be recognized, an update will be forced.")
            }
        }
        forceUpdateCommands(guild)
    }

    private fun createCommandMismatchMessage(preMessage: String): String = """
        $preMessage
        Please check if you have another bot instance running as it could have replaced the current command set.
        Do not share your tokens with anyone else (even your friend), and use a separate token when testing.
        If the problem persists, please report the issue. ${getDiagnosticVersions()}
    """.trimIndent()

    private fun printAvailableCommands(event: GenericCommandInteractionEvent) {
        logger.debug {
            val guild = event.guild
            val topLevelCommands = applicationCommandsContext.getEffectiveApplicationCommands(guild)
            val scopeName = when (guild?.isDetached) {
                false -> "'${guild.name}'"
                // Detached guild == global command
                else -> "Global scope"
            }
            val availableCommands = buildString {
                topLevelCommands
                    .sortedBy { it.name }
                    .forEach { command ->
                        if (command is TopLevelSlashCommandInfo) {
                            appendLine(" - /${command.name}")
                            command.subcommands.values.forEach { subcommand ->
                                appendLine("${" ".repeat(4)} - ${subcommand.name}")
                            }

                            command.subcommandGroups.values.forEach { subcommandGroup ->
                                appendLine("${" ".repeat(4)} - ${subcommandGroup.name}")
                                subcommandGroup.subcommands.values.forEach { subcommand ->
                                    appendLine("${" ".repeat(8)} - ${subcommand.name}")
                                }
                            }
                        } else {
                            appendLine(" - ${command.name}")
                        }
                    }
            }
            "Commands available in $scopeName:\n$availableCommands"
        }
    }

    private fun forceUpdateCommands(guild: Guild?) {
        if (guild?.isDetached == false) {
            applicationCommandsContext.updateGuildApplicationCommands(guild, force = true).whenComplete { _, e ->
                if (e != null)
                    logger.error(e) { "An exception occurred while trying to update commands of guild '${guild.name}' (${guild.id}) after a command was missing" }
            }
        }
        applicationCommandsContext.updateGlobalApplicationCommands(force = true).whenComplete { _, e ->
            if (e != null)
                logger.error(e) { "An exception occurred while trying to update global commands after a command was missing" }
        }
    }

    private suspend fun handleException(e: Throwable, event: GenericCommandInteractionEvent, cmdType: CommandType, context: Map<String, Any?>) {
        if (e is CancellationException)
            return logger.trace(e) { "${cmdType.asString.replaceFirstChar { it.uppercase() }} '${event.commandString}' was cancelled" }

        val logLevel = if (e is OptionNotFoundException) {
            logger.warn { createCommandMismatchMessage("An option could not be found, commands will be force updated.") }
            forceUpdateCommands(event.guild)
            Level.DEBUG
        } else {
            Level.ERROR
        }

        exceptionHandler.handleException(event, e, "${cmdType.asString} '${event.commandString}'", context, logLevel)
        if (e is InsufficientPermissionException) {
            event.replyExceptionMessage(messagesFactory.get(event).missingBotPermissions(event, setOf(e.permission)))
        } else {
            event.replyExceptionMessage(messagesFactory.get(event).uncaughtException(event))
        }
    }

    private suspend fun canRun(event: GenericCommandInteractionEvent, applicationCommand: ApplicationCommandInfoImpl): Boolean {
        val usability = applicationCommand.getUsability(event.inputUser, event.messageChannel)
        if (usability.isNotUsable) {
            val errorMessage = fromMessages(event) {
                when (usability.bestReason) {
                    UnusableReason.OWNER_ONLY -> throwInternal("Application commands can't be owner-only")
                    UnusableReason.USER_PERMISSIONS -> {
                        val member = event.member ?: throwInternal("USER_PERMISSIONS got checked even if guild is null")
                        val missingPermissions = getMissingPermissions(applicationCommand.userPermissions, member, event.guildChannel)
                        missingUserPermissions(event, missingPermissions)
                    }
                    UnusableReason.BOT_PERMISSIONS -> {
                        val guild = event.guild ?: throwInternal("BOT_PERMISSIONS got checked even if guild is null")
                        val missingPermissions = getMissingPermissions(applicationCommand.botPermissions, guild.selfMember, event.guildChannel)
                        missingBotPermissions(event, missingPermissions)
                    }
                    UnusableReason.NSFW_ONLY -> throwInternal("Discord already handles NSFW commands")
                    UnusableReason.HIDDEN -> throwInternal("Application commands can't be hidden")
                }
            }
            reply(event, errorMessage)
            return false
        }

        checkFilters(globalFilters, applicationCommand.filters) { filter ->
            val rejectionReason = filter.checkSuspend(event, applicationCommand)
            if (rejectionReason != null) {
                if (event.isAcknowledged) {
                    logger.trace { "${filter.description} rejected application command '${event.commandString}' by user ${event.user.id}: $rejectionReason" }
                } else {
                    logger.warn { "${filter.description} rejected application command '${event.commandString}' by user ${event.user.id} but did not acknowledge the interaction: $rejectionReason" }
                }
                return false
            }
        }

        return true
    }

    private fun reply(event: GenericCommandInteractionEvent, message: MessageCreateData) {
        event.reply(message)
            .setEphemeral(true)
            .queue(null) { throwable ->
                exceptionHandler.handleException(event, throwable, "interaction reply", emptyMap())
            }
    }

    private inline fun fromMessages(event: Interaction, crossinline block: ApplicationCommandsMessages.() -> MessageCreateData): MessageCreateData {
        return messagesFactory.get(event).run(block)
    }

    private enum class CommandType(val asString: String) {
        APPLICATION("application"),
        SLASH("slash"),
        MESSAGE_CONTEXT("message context"),
        USER_CONTEXT("user context"),
    }
}
