package com.freya02.botcommands.internal.commands.prefixed

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.CooldownScope
import com.freya02.botcommands.api.commands.prefixed.TextFilteringData
import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.Usability
import com.freya02.botcommands.internal.Usability.UnusableReason
import com.freya02.botcommands.internal.core.CooldownService
import com.freya02.botcommands.internal.getDeepestCause
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.internal.requests.CompletedRestAction
import java.util.regex.Matcher

private data class CommandWithArgs(val command: TextCommandInfo, val args: String)

internal class TextCommandsListener(private val context: BContextImpl, private val cooldownService: CooldownService, private val helpCommandInfo: HelpCommandInfo?) {
    private val logger = KotlinLogging.logger {  }
    private val spacePattern = Regex("\\s+")

    @BEventListener
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
            context.config.textConfig.usePingAsPrefix && msg.startsWith(event.jda.selfUser.asMention) -> msg.substringAfter(' ')
            else -> getMsgNoPrefix(msg, event.guild)
        }
        if (content.isNullOrBlank()) return

        logger.trace { "Received prefixed command: $msg" }

        context.config.coroutineScopesConfig.textCommandsScope.launch {
            try {
                val isNotOwner = !context.config.isOwner(member.idLong)

                val (commandInfo: TextCommandInfo, args: String) = findCommandWithArgs(content) ?: let {
//                onCommandNotFound(event, CommandPath.of(words[0]), isNotOwner)
                    return@launch
                }

                commandInfo.variations.forEach {
                    when (it.completePattern) {
                        null -> { //Fallback method
                            if (tryExecute(event, args, it, isNotOwner, null) != ExecutionResult.CONTINUE) return@launch
                        }
                        else -> { //Regex text command
                            val matcher = it.completePattern.matcher(args)
                            if (matcher.matches()) {
                                if (tryExecute(event, args, it, isNotOwner, matcher) != ExecutionResult.CONTINUE) return@launch
                            }
                        }
                    }
                }

                helpCommandInfo?.let { (helpCommand, _) ->
                    helpCommand.onInvalidCommand(
                        BaseCommandEventImpl(context, event, ""),
                        commandInfo
                    )
                }
            } catch (e: Throwable) {
                handleException(event, e, msg)
            }
        }
    }

    private fun handleException(event: MessageReceivedEvent, e: Throwable, msg: String) {
        val handler = context.uncaughtExceptionHandler
        if (handler != null) {
            handler.onException(context, event, e)
            return
        }

        val baseEx = e.getDeepestCause()

        logger.error("Unhandled exception while executing a text command '$msg'", baseEx)

        replyError(event, context.getDefaultMessages(event.guild).generalErrorMsg)

        context.dispatchException("Exception in text command '$msg'", baseEx)
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
            CommandWithArgs(it, words.drop(it.path.nameCount).joinToString(" "))
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

        return context.config.textConfig.prefixes
    }

    private suspend fun tryExecute(
        event: MessageReceivedEvent,
        args: String,
        variation: TextCommandVariation,
        isNotOwner: Boolean,
        matcher: Matcher?
    ): ExecutionResult {
        val commandInfo = variation.info

        val filteringData = TextFilteringData(context, event, commandInfo, args)
        for (filter in context.config.textConfig.textFilters) {
            if (!filter.isAccepted(filteringData)) {
                logger.trace("Cancelled prefixed commands due to filter")
                return ExecutionResult.STOP
            }
        }

        val usability = Usability.of(context, commandInfo, event.member!!, event.guildChannel, isNotOwner)

        if (usability.isUnusable) {
            val unusableReasons = usability.unusableReasons
            if (unusableReasons.contains(UnusableReason.HIDDEN)) {
                onCommandNotFound(event, commandInfo.path, true)
                return ExecutionResult.STOP
            } else if (unusableReasons.contains(UnusableReason.OWNER_ONLY)) {
                replyError(event, context.getDefaultMessages(event.guild).ownerOnlyErrorMsg)
                return ExecutionResult.STOP
            } else if (unusableReasons.contains(UnusableReason.NSFW_DISABLED)) {
                replyError(event, context.getDefaultMessages(event.guild).nsfwDisabledErrorMsg)
                return ExecutionResult.STOP
            } else if (unusableReasons.contains(UnusableReason.NSFW_ONLY)) {
                replyError(event, context.getDefaultMessages(event.guild).nsfwOnlyErrorMsg)
                return ExecutionResult.STOP
            } else if (unusableReasons.contains(UnusableReason.NSFW_DM_DENIED)) {
                replyError(event, context.getDefaultMessages(event.guild).nsfwdmDeniedErrorMsg)
                return ExecutionResult.STOP
            } else if (unusableReasons.contains(UnusableReason.USER_PERMISSIONS)) {
                replyError(event, context.getDefaultMessages(event.guild).userPermErrorMsg)
                return ExecutionResult.STOP
            } else if (unusableReasons.contains(UnusableReason.BOT_PERMISSIONS)) {
                val missingPermsStr =
                    (commandInfo.botPermissions - event.guild.selfMember.getPermissions(event.guildChannel)).joinToString {
                        it.name
                    }

                replyError(event, context.getDefaultMessages(event.guild).getBotPermErrorMsg(missingPermsStr))
                return ExecutionResult.STOP
            }
        }

        if (isNotOwner) {
            val cooldown: Long = cooldownService.getCooldown(commandInfo, event)
            if (cooldown > 0) {
                val defaultMessages = context.getDefaultMessages(event.guild)
                when (commandInfo.cooldownStrategy.scope) {
                    CooldownScope.USER -> replyError(event, defaultMessages.getUserCooldownMsg(cooldown / 1000.0))
                    CooldownScope.GUILD -> replyError(event, defaultMessages.getGuildCooldownMsg(cooldown / 1000.0))
                    CooldownScope.CHANNEL -> replyError(event, defaultMessages.getChannelCooldownMsg(cooldown / 1000.0))
                }

                return ExecutionResult.STOP
            }
        }

        return variation.execute(event, cooldownService, args, matcher)
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
                .handle(Exception::class.java) { e: Exception ->
                    logger.error("Could not send reply message from command listener", e)
                    context.dispatchException("Could not send reply message from command listener", e)
                })
    }

    private fun onCommandNotFound(event: MessageReceivedEvent, commandName: CommandPath, isNotOwner: Boolean) {
        val suggestions = getSuggestions(event, commandName, isNotOwner)
        if (suggestions.isNotEmpty()) {
            replyError(
                event,
                context.getDefaultMessages(event.guild).getCommandNotFoundMsg(suggestions.joinToString("**, **", "**", "**"))
            )
        }
    }

    private fun getSuggestions(event: MessageReceivedEvent, triedCommandPath: CommandPath, isNotOwner: Boolean): List<String> {
        return listOf() //TODO decide if useful or not
    }
}