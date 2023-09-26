package com.freya02.botcommands.internal.commands.prefixed

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.prefixed.BaseCommandEvent
import com.freya02.botcommands.api.commands.prefixed.IHelpCommand
import com.freya02.botcommands.api.commands.prefixed.TextCommandFilter
import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.getInterfacedServices
import com.freya02.botcommands.api.core.utils.getMissingPermissions
import com.freya02.botcommands.api.core.utils.simpleNestedName
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.ExceptionHandler
import com.freya02.botcommands.internal.Usability
import com.freya02.botcommands.internal.Usability.UnusableReason
import com.freya02.botcommands.internal.commands.withRateLimit
import com.freya02.botcommands.internal.utils.throwInternal
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.internal.requests.CompletedRestAction

private val logger = KotlinLogging.logger { }
private val spacePattern = Regex("\\s+")

@BService
internal class TextCommandsListener internal constructor(
    private val context: BContextImpl,
    private val helpCommand: IHelpCommand?
) {
    private data class CommandWithArgs(val command: TextCommandInfo, val args: String)

    private val exceptionHandler = ExceptionHandler(context, logger)

    private val filters = context.getInterfacedServices<TextCommandFilter>()

    @BEventListener(ignoreIntents = true)
    suspend fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot || event.isWebhookMessage) return

        if (!event.isFromGuild) return

        val member = event.member ?: let {
            logger.error("Command caller member is null ! This shouldn't happen if the message isn't a webhook, or is the docs wrong ?")
            return
        }

        val isBotMentioned = event.message.mentions.isMentioned(event.jda.selfUser)
        if (GatewayIntent.MESSAGE_CONTENT !in event.jda.gatewayIntents && !isBotMentioned) return

        val msg: String = event.message.contentRaw
        val content = when {
            context.textConfig.usePingAsPrefix && msg.startsWith(event.jda.selfUser.asMention) -> msg.substringAfter(' ', missingDelimiterValue = "")
            else -> getMsgNoPrefix(msg, event.guild)
        }
        if (content.isNullOrBlank()) return

        logger.trace { "Received prefixed command: $msg" }

        context.coroutineScopesConfig.textCommandsScope.launch {
            try {
                val isNotOwner = !context.config.isOwner(member.idLong)

                val (commandInfo: TextCommandInfo, args: String) = findCommandWithArgs(content) ?: let {
                    // At this point no top level command was found,
                    // if a subcommand wasn't matched, it would simply appear in the args
                    onCommandNotFound(event, CommandPath.of(content.substringBefore(' ')), isNotOwner)
                    return@launch
                }

                commandInfo.withRateLimit(context, event, isNotOwner) {
                    if (!canRun(event, commandInfo, isNotOwner)) {
                        false
                    } else {
                        tryVariations(event, commandInfo, content, args)
                    }
                }
            } catch (e: Throwable) {
                handleException(event, e, msg)
            }
        }
    }

    private suspend fun tryVariations(
        event: MessageReceivedEvent,
        commandInfo: TextCommandInfo,
        content: String,
        args: String
    ): Boolean {
        commandInfo.variations.forEach {
            val bcEvent = it.createEvent(event, args)

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

        helpCommand?.onInvalidCommand(BaseCommandEventImpl(context, event, ""), commandInfo)
        return false
    }

    private fun handleException(event: MessageReceivedEvent, e: Throwable, msg: String) {
        exceptionHandler.handleException(event, e, "text command '$msg'")
        replyError(event, context.getDefaultMessages(event.guild).generalErrorMsg)
    }

    private fun findCommandWithArgs(content: String): CommandWithArgs? {
        var commandInfo: TextCommandInfo? = null
        val words: List<String> = spacePattern.split(content)
        for (index in words.indices) {
            when (val info = context.textCommandsContext.findTextCommand(words.subList(0, index + 1))) {
                null -> break
                else -> commandInfo = info
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

    private fun canRun(event: MessageReceivedEvent, commandInfo: TextCommandInfo, isNotOwner: Boolean): Boolean {
        val member = event.member ?: throwInternal("Text command was executed out of a Guild")
        val usability = Usability.of(context, commandInfo, member, event.guildChannel, isNotOwner)

        if (usability.isUnusable) {
            val unusableReasons = usability.unusableReasons
            if (unusableReasons.contains(UnusableReason.HIDDEN)) {
                onCommandNotFound(event, commandInfo.path, true)
                return false
            } else if (unusableReasons.contains(UnusableReason.OWNER_ONLY)) {
                replyError(event, context.getDefaultMessages(event.guild).ownerOnlyErrorMsg)
                return false
            } else if (unusableReasons.contains(UnusableReason.NSFW_DISABLED)) {
                replyError(event, context.getDefaultMessages(event.guild).nsfwDisabledErrorMsg)
                return false
            } else if (unusableReasons.contains(UnusableReason.NSFW_ONLY)) {
                replyError(event, context.getDefaultMessages(event.guild).nsfwOnlyErrorMsg)
                return false
            } else if (unusableReasons.contains(UnusableReason.NSFW_DM_DENIED)) {
                replyError(event, context.getDefaultMessages(event.guild).nsfwdmDeniedErrorMsg)
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
        variation: TextCommandVariation,
        matchResult: MatchResult?
    ): ExecutionResult {
        val optionValues = variation.tryParseOptionValues(event, args, matchResult)
            ?: return ExecutionResult.CONTINUE //Go to next variation

        // At this point, we're sure that the command is executable
        for (filter in filters) {
            if (!filter.isAcceptedSuspend(event, variation, args)) {
                logger.trace { "${filter::class.simpleNestedName} rejected text command '$content'" }
                return ExecutionResult.STOP
            }
        }

        return variation.execute(event, optionValues)
    }

    private fun replyError(event: MessageReceivedEvent, msg: String) {
        val action = when {
            event.guildChannel.canTalk() -> CompletedRestAction(event.jda, event.channel)
            else -> event.author.openPrivateChannel()
        }

        action
            .flatMap { it.sendMessage(msg) }
            .queue(null, ErrorHandler()
                .ignore(ErrorResponse.CANNOT_SEND_TO_USER)
                .handle(Throwable::class.java) {
                    exceptionHandler.handleException(event, it, "text command error reply")
                })
    }

    private fun onCommandNotFound(event: MessageReceivedEvent, commandName: CommandPath, isNotOwner: Boolean) {
        if (!context.textConfig.showSuggestions) return

        TODO("Suggestions are not implemented yet")

//        val suggestions = getSuggestions(event, commandName, isNotOwner)
//        if (suggestions.isNotEmpty()) {
//            replyError(
//                event,
//                context.getDefaultMessages(event.guild).getCommandNotFoundMsg(suggestions.joinToString("**, **", "**", "**"))
//            )
//        }
    }

//    private fun getSuggestions(event: MessageReceivedEvent, triedCommandPath: CommandPath, isNotOwner: Boolean): List<String> {
//        return listOf() //TODO decide if useful or not
//    }
}