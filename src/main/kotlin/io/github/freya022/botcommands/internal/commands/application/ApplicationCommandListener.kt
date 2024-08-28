package io.github.freya022.botcommands.internal.commands.application

import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.Usability.UnusableReason
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandRejectionHandler
import io.github.freya022.botcommands.api.commands.application.annotations.RequiresApplicationCommands
import io.github.freya022.botcommands.api.commands.application.context.message.GlobalMessageEvent
import io.github.freya022.botcommands.api.commands.application.context.message.GuildMessageEvent
import io.github.freya022.botcommands.api.commands.application.context.user.GlobalUserEvent
import io.github.freya022.botcommands.api.commands.application.context.user.GuildUserEvent
import io.github.freya022.botcommands.api.commands.application.diff.DiffEngine
import io.github.freya022.botcommands.api.commands.application.getApplicationCommandById
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.TopLevelSlashCommandInfo
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.checkFilters
import io.github.freya022.botcommands.api.core.config.application.cache.ApplicationCommandsCacheConfigBuilder
import io.github.freya022.botcommands.api.core.entities.inputUser
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.getMissingPermissions
import io.github.freya022.botcommands.api.localization.DefaultMessagesFactory
import io.github.freya022.botcommands.internal.commands.application.cache.factory.ApplicationCommandsCacheFactory
import io.github.freya022.botcommands.internal.commands.application.context.message.MessageCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.application.context.user.UserCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.application.slash.SlashCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.application.slash.exceptions.OptionNotFoundException
import io.github.freya022.botcommands.internal.commands.ratelimit.withRateLimit
import io.github.freya022.botcommands.internal.core.ExceptionHandler
import io.github.freya022.botcommands.internal.core.exceptions.getDiagnosticVersions
import io.github.freya022.botcommands.internal.localization.interaction.LocalizableInteractionFactory
import io.github.freya022.botcommands.internal.utils.*
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.Level
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException

private val logger = KotlinLogging.logger {  }

@BService
@RequiresApplicationCommands
internal class ApplicationCommandListener internal constructor(
    private val context: BContext,
    private val applicationCommandsBuilder: ApplicationCommandsBuilder,
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
            val slashCommand = context.applicationCommandsContext
                .getApplicationCommandById<SlashCommandInfoImpl>(event.commandIdLong, event.subcommandGroup, event.subcommandName)
                ?: return@launch onCommandNotFound(event, "A slash command could not be found: ${event.fullCommandName}")

            val isNotOwner = event.user !in context.botOwners
            slashCommand.withRateLimit(context, event, isNotOwner) { cancellableRateLimit ->
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
    }

    @BEventListener
    suspend fun onUserContextCommand(event: UserContextInteractionEvent) {
        logger.trace { "Received user context command: ${event.name}" }

        scope.launchCatching({ handleException(it, event) }) launch@{
            val userCommand = context.applicationCommandsContext
                .getApplicationCommandById<UserCommandInfoImpl>(event.commandIdLong, group = null, subcommand = null)
                ?: return@launch onCommandNotFound(event, "A user context command could not be found: ${event.name}")

            val isNotOwner = event.user !in context.botOwners
            userCommand.withRateLimit(context, event, isNotOwner) { cancellableRateLimit ->
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
    }

    @BEventListener
    suspend fun onMessageContextCommand(event: MessageContextInteractionEvent) {
        logger.trace { "Received message context command: ${event.name}" }

        scope.launchCatching({ handleException(it, event) }) launch@{
            val messageCommand = context.applicationCommandsContext
                .getApplicationCommandById<MessageCommandInfoImpl>(event.commandIdLong, group = null, subcommand = null)
                ?: return@launch onCommandNotFound(event, "A message context command could not be found: ${event.name}")

            val isNotOwner = event.user !in context.botOwners
            messageCommand.withRateLimit(context, event, isNotOwner) { cancellableRateLimit ->
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
            return event.reply_(defaultMessagesFactory.get(event).applicationCommandsNotAvailableMsg, ephemeral = true).queue()
        }

        //This is done so warnings are printed after the exception
        handleException(IllegalArgumentException(message), event)
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

    @Suppress("DEPRECATION")
    private fun createCommandMismatchMessage(preMessage: String): String = """
        $preMessage
        Please check if you have another bot instance running as it could have replaced the current command set.
        Do not share your tokens with anyone else (even your friend), and use a separate token when testing.
        If the problem persists, try changing the diff engine in ${ApplicationCommandsCacheConfigBuilder::diffEngine.reference} to ${DiffEngine.OLD} and report the issue. ${getDiagnosticVersions()}
    """.trimIndent()

    private fun printAvailableCommands(event: GenericCommandInteractionEvent) {
        logger.debug {
            val guild = event.guild
            val topLevelCommands = context.applicationCommandsContext.getEffectiveApplicationCommands(guild)
            val scopeName = if (guild != null) "'" + guild.name + "'" else "Global scope"
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
        if (guild != null) {
            context.applicationCommandsContext.updateGuildApplicationCommands(guild, force = true).whenComplete { _, e ->
                if (e != null)
                    logger.error(e) { "An exception occurred while trying to update commands of guild '${guild.name}' (${guild.id}) after a command was missing" }
            }
        }
        context.applicationCommandsContext.updateGlobalApplicationCommands(force = true).whenComplete { _, e ->
            if (e != null)
                logger.error(e) { "An exception occurred while trying to update global commands after a command was missing" }
        }
    }

    private suspend fun handleException(e: Throwable, event: GenericCommandInteractionEvent) {
        val logLevel = if (e is OptionNotFoundException) {
            logger.warn { createCommandMismatchMessage("An option could not be found, commands will be force updated.") }
            forceUpdateCommands(event.guild)
            Level.DEBUG
        } else {
            Level.ERROR
        }

        exceptionHandler.handleException(event, e, "application command '${event.commandString}'", emptyMap(), logLevel)
        if (e is InsufficientPermissionException) {
            event.replyExceptionMessage(defaultMessagesFactory.get(event).getBotPermErrorMsg(setOf(e.permission)))
        } else {
            event.replyExceptionMessage(defaultMessagesFactory.get(event).generalErrorMsg)
        }
    }

    private suspend fun canRun(event: GenericCommandInteractionEvent, applicationCommand: ApplicationCommandInfoImpl): Boolean {
        val usability = applicationCommand.getUsability(event.inputUser, event.messageChannel)
        if (usability.isNotUsable) {
            val errorMessage: String = when (usability.bestReason) {
                UnusableReason.OWNER_ONLY -> defaultMessagesFactory.get(event).ownerOnlyErrorMsg
                UnusableReason.USER_PERMISSIONS -> {
                    val member = event.member ?: throwInternal("USER_PERMISSIONS got checked even if guild is null")
                    val missingPermissions = getMissingPermissions(applicationCommand.userPermissions, member, event.guildChannel)
                    defaultMessagesFactory.get(event).getUserPermErrorMsg(missingPermissions)
                }
                UnusableReason.BOT_PERMISSIONS -> {
                    val guild = event.guild ?: throwInternal("BOT_PERMISSIONS got checked even if guild is null")
                    val missingPermissions = getMissingPermissions(applicationCommand.botPermissions, guild.selfMember, event.guildChannel)
                    defaultMessagesFactory.get(event).getBotPermErrorMsg(missingPermissions)
                }
                UnusableReason.NSFW_ONLY -> throwInternal("Discord already handles NSFW commands")
                UnusableReason.HIDDEN -> throwInternal("Application commands can't be hidden")
            }
            reply(event, errorMessage)
            return false
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