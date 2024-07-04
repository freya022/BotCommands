package io.github.freya022.botcommands.internal.commands.text

import dev.minn.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.commands.ratelimit.CancellableRateLimit
import io.github.freya022.botcommands.api.commands.text.*
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.checkFilters
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.getMissingPermissions
import io.github.freya022.botcommands.api.core.utils.runIgnoringResponse
import io.github.freya022.botcommands.internal.commands.Usability
import io.github.freya022.botcommands.internal.commands.Usability.UnusableReason
import io.github.freya022.botcommands.internal.commands.ratelimit.withRateLimit
import io.github.freya022.botcommands.internal.core.ExceptionHandler
import io.github.freya022.botcommands.internal.localization.text.LocalizableTextCommandFactory
import io.github.freya022.botcommands.internal.utils.*
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.requests.GatewayIntent

private val logger = KotlinLogging.logger { }
private val spacePattern = Regex("\\s+")

@BService
internal class TextCommandsListener internal constructor(
    private val context: BContext,
    private val textCommandsContext: TextCommandsContextImpl,
    private val localizableTextCommandFactory: LocalizableTextCommandFactory,
    filters: List<TextCommandFilter<Any>>,
    rejectionHandler: TextCommandRejectionHandler<Any>?,
    private val suggestionSupplier: TextSuggestionSupplier = DefaultTextSuggestionSupplier,
    private val helpCommand: IHelpCommand?
) {
    private data class CommandWithArgs(val command: TextCommandInfoImpl, val args: String)

    private val scope = context.coroutineScopesConfig.textCommandsScope
    private val exceptionHandler = ExceptionHandler(context, logger)

    // Types are crosschecked anyway
    private val globalFilters: List<TextCommandFilter<Any>> = filters.filter { it.global }
    private val rejectionHandler: TextCommandRejectionHandler<Any>? = when {
        globalFilters.isEmpty() -> null
        else -> rejectionHandler
            ?: throwState("A ${classRef<TextCommandRejectionHandler<*>>()} must be available if ${classRef<TextCommandFilter<*>>()} is used")
    }

    @BEventListener(ignoreIntents = true)
    suspend fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot || event.isWebhookMessage) return

        if (!event.isFromGuild) return

        val isBotMentioned = event.message.mentions.isMentioned(event.jda.selfUser)
        if (GatewayIntent.MESSAGE_CONTENT !in event.jda.gatewayIntents && !isBotMentioned) return

        val msg: String = event.message.contentRaw
        val content = when {
            context.textConfig.usePingAsPrefix && msg.startsWith(event.jda.selfUser.asMention) -> msg.substringAfter(' ', missingDelimiterValue = "")
            else -> getMsgNoPrefix(msg, event.guild)
        }
        if (content.isNullOrBlank()) return

        logger.trace { "Received text command: $msg" }

        scope.launchCatching({ handleException(event, it, msg) }) launch@{
            val member = event.member ?: throwInternal("Command caller member is null ! This shouldn't happen if the message isn't a webhook, or is the docs wrong ?")
            val isNotOwner = !context.config.isOwner(member.idLong)

            val (commandInfo: TextCommandInfoImpl, args: String) = findCommandWithArgs(content, isNotOwner) ?: let {
                // At this point no top level command was found,
                // if a subcommand wasn't matched, it would simply appear in the args
                onCommandNotFound(event, content.substringBefore(' '), isNotOwner)
                return@launch
            }

            logger.trace { "Detected text command '${commandInfo.path}' with args '$args'" }

            commandInfo.withRateLimit(context, event, isNotOwner) { cancellableRateLimit ->
                if (!canRun(event, commandInfo, isNotOwner)) {
                    false
                } else {
                    tryVariations(event, commandInfo, content, args, cancellableRateLimit)
                }
            }
        }
    }

    private suspend fun tryVariations(
        event: MessageReceivedEvent,
        commandInfo: TextCommandInfoImpl,
        content: String,
        args: String,
        cancellableRateLimit: CancellableRateLimit
    ): Boolean {
        val localizableTextCommand = localizableTextCommandFactory.create(event)
        commandInfo.variations.forEach {
            val bcEvent = it.createEvent(event, args, cancellableRateLimit, localizableTextCommand)

            // null on a fallback command
            val pattern = it.completePattern

            val executionResult = if (pattern == null) {
                //Fallback method
                tryExecute(bcEvent, content, args, it, null)
            } else {
                //Regex text command
                val matchResult = pattern.matchEntire(args)
                if (matchResult != null) {
                    tryExecute(bcEvent, content, args, it, matchResult)
                } else {
                    ExecutionResult.CONTINUE
                }
            }

            when (executionResult) {
                ExecutionResult.CONTINUE -> return@forEach //Check other variations
                ExecutionResult.STOP -> return false
                ExecutionResult.OK -> return true
            }
        }

        helpCommand?.onInvalidCommandSuspend(BaseCommandEventImpl(context, event, "", cancellableRateLimit, localizableTextCommand), commandInfo)
        return false
    }

    private suspend fun handleException(event: MessageReceivedEvent, e: Throwable, msg: String) {
        exceptionHandler.handleException(event, e, "text command '$msg'", mapOf("Message" to event.jumpUrl))
        if (e is InsufficientPermissionException) {
            replyError(event, context.getDefaultMessages(event.guild).getBotPermErrorMsg(setOf(e.permission)))
        } else {
            replyError(event, context.getDefaultMessages(event.guild).generalErrorMsg)
        }
    }

    private fun findCommandWithArgs(content: String, isNotOwner: Boolean): CommandWithArgs? {
        var commandInfo: TextCommandInfoImpl? = null
        val words: List<String> = spacePattern.split(content)
        for (index in words.indices) {
            when (val info = textCommandsContext.findTextCommand(words.subList(0, index + 1))) {
                null -> break
                else -> {
                    if (info.hidden && isNotOwner) {
                        //This will help us have the same behavior as if the command didn't exist
                        continue
                    } else {
                        commandInfo = info
                    }
                }
            }
        }

        return commandInfo?.let {
            //Keep the part after the command paths
            val args = (0..<commandInfo.path.nameCount).fold(content) { acc, i ->
                acc.substringAfter(words[i])
            }.trimStart()
            CommandWithArgs(it, args)
        }
    }

    private fun getMsgNoPrefix(msg: String, guild: Guild): String? {
        return getPrefixes(guild)
            .find { prefix -> msg.startsWith(prefix) }
            ?.let { prefix -> msg.substring(prefix.length).trim() }
    }

    private fun getPrefixes(guild: Guild): List<String> {
        context.settingsProvider?.let { settingsProvider ->
            val prefixes = settingsProvider.getPrefixes(guild)
            if (!prefixes.isNullOrEmpty()) return prefixes
        }

        return context.textConfig.prefixes
    }

    private suspend fun canRun(event: MessageReceivedEvent, commandInfo: TextCommandInfo, isNotOwner: Boolean): Boolean {
        val member = event.member ?: throwInternal("Text command was executed out of a Guild")
        val usability = Usability.of(context, commandInfo, member, event.guildChannel, isNotOwner)

        if (usability.isUnusable) {
            val unusableReasons = usability.unusableReasons
            if (unusableReasons.contains(UnusableReason.HIDDEN)) {
                throwInternal("Hidden commands should have been ignored by ${TextCommandsListener::findCommandWithArgs.shortSignature}")
            } else if (unusableReasons.contains(UnusableReason.OWNER_ONLY)) {
                replyError(event, context.getDefaultMessages(event.guild).ownerOnlyErrorMsg)
                return false
            } else if (unusableReasons.contains(UnusableReason.NSFW_ONLY)) {
                replyError(event, context.getDefaultMessages(event.guild).nsfwOnlyErrorMsg)
                return false
            } else if (unusableReasons.contains(UnusableReason.USER_PERMISSIONS)) {
                val missingPermissions = getMissingPermissions(commandInfo.userPermissions, member, event.guildChannel)
                replyError(event, context.getDefaultMessages(event.guild).getUserPermErrorMsg(missingPermissions))
                return false
            } else if (unusableReasons.contains(UnusableReason.BOT_PERMISSIONS)) {
                val missingPermissions = getMissingPermissions(commandInfo.botPermissions, event.guild.selfMember, event.guildChannel)
                replyError(event, context.getDefaultMessages(event.guild).getBotPermErrorMsg(missingPermissions))
                return false
            }
        }

        return true
    }

    private suspend fun tryExecute(
        event: BaseCommandEvent,
        content: String,
        args: String,
        variation: TextCommandVariationImpl,
        matchResult: MatchResult?
    ): ExecutionResult {
        val optionValues = variation.tryParseOptionValues(event, args, matchResult)
            ?: return ExecutionResult.CONTINUE //Go to next variation

        // At this point, we're sure that the command is executable
        checkFilters(globalFilters, variation.filters) { filter ->
            val userError = filter.checkSuspend(event, variation, args)
            if (userError != null) {
                rejectionHandler!!.handleSuspend(event, variation, args, userError)
                logger.trace { "${filter.description} rejected text command '$content'" }
                return ExecutionResult.STOP
            }
        }

        variation.execute(event, optionValues)
        return ExecutionResult.OK
    }

    private suspend fun replyError(event: MessageReceivedEvent, msg: String) {
        val channel = when {
            event.guildChannel.canTalk() -> event.channel
            else -> event.author.openPrivateChannel().await()
        }

        runIgnoringResponse(ErrorResponse.CANNOT_SEND_TO_USER) {
            channel.sendMessage(msg).await()
        }
    }

    private suspend fun onCommandNotFound(event: MessageReceivedEvent, commandName: String, isNotOwner: Boolean) {
        if (!context.textConfig.showSuggestions) return

        val candidates = context.textCommandsContext.rootCommands
            .filter { Usability.of(context, it, event.member!!, event.guildChannel, isNotOwner = isNotOwner).isShowable }

        val suggestions = suggestionSupplier.getSuggestions(commandName, candidates)
        if (suggestions.isNotEmpty()) {
            val suggestionsStr = suggestions.joinToString("**, **", "**", "**") { it.name }
            replyError(event, context.getDefaultMessages(event.guild).getCommandNotFoundMsg(suggestionsStr))
        }
    }
}