package io.github.freya022.botcommands.internal.commands.text

import dev.freya02.botcommands.jda.ktx.coroutines.await
import dev.freya02.botcommands.jda.ktx.messages.suppressContentWarning
import dev.freya02.botcommands.jda.ktx.requests.awaitCatching
import dev.freya02.botcommands.jda.ktx.requests.handle
import io.github.freya022.botcommands.api.commands.Usability.UnusableReason
import io.github.freya022.botcommands.api.commands.text.*
import io.github.freya022.botcommands.api.commands.text.annotations.RequiresTextCommands
import io.github.freya022.botcommands.api.commands.text.messages.TextCommandsMessages
import io.github.freya022.botcommands.api.commands.text.messages.TextCommandsMessagesFactory
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.JDAService
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.checkFilters
import io.github.freya022.botcommands.api.core.config.BTextConfig
import io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.service.getServiceOrNull
import io.github.freya022.botcommands.api.core.utils.getMissingPermissions
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.ratelimit.CancellableRateLimit
import io.github.freya022.botcommands.internal.commands.text.TextCommandsListener.Status.*
import io.github.freya022.botcommands.internal.commands.text.ratelimit.TextCommandRateLimitHandler
import io.github.freya022.botcommands.internal.core.ExceptionHandler
import io.github.freya022.botcommands.internal.localization.text.LocalizableTextCommandFactory
import io.github.freya022.botcommands.internal.utils.reference
import io.github.freya022.botcommands.internal.utils.shortSignature
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import kotlin.coroutines.cancellation.CancellationException

private val logger = KotlinLogging.logger { }
private val spacePattern = Regex("\\s+")

@BService
@RequiresTextCommands
@ConditionalService(TextCommandsListener.ActivationCondition::class)
internal class TextCommandsListener internal constructor(
    private val context: BContext,
    private val textConfig: BTextConfig,
    private val messagesFactory: TextCommandsMessagesFactory,
    private val textCommandsContext: TextCommandsContextImpl,
    private val localizableTextCommandFactory: LocalizableTextCommandFactory,
    private val rateLimitHandler: TextCommandRateLimitHandler,
    filters: List<TextCommandFilter>,
    private val suggestionSupplier: TextSuggestionSupplier,
    private val helpCommand: IHelpCommand?
) {
    private data class CommandWithArgs(val command: TextCommandInfoImpl, val args: String)

    private val scope = context.coroutineScopesConfig.textCommandsScope
    private val exceptionHandler = ExceptionHandler(context, logger)

    private val globalFilters = filters.filter { it.global }

    @BEventListener(ignoreIntents = true)
    fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot || event.isWebhookMessage || event.message.type.isSystem) return

        if (!event.isFromGuild) return

        // Could also check mentions, but this is way easier and faster
        val msg: String = suppressContentWarning { event.message.contentRaw }
        val content = getMsgNoPrefix(msg, event.guildChannel)
        if (content.isNullOrBlank()) return

        logger.trace { "Received text command: $msg" }

        scope.launch {
            try {
                handleTextCommands(event, content)
            } catch (e: Exception) {
                handleException(event, e, msg)
            }
        }
    }

    private suspend fun handleTextCommands(event: MessageReceivedEvent, content: String) {
        val isNotOwner = event.author !in context.botOwners
        val (commandInfo: TextCommandInfoImpl, args: String) = findCommandWithArgs(content, isNotOwner) ?: let {
            // At this point no top level command was found,
            // if a subcommand wasn't matched, it would simply appear in the args
            onCommandNotFound(event, content.substringBefore(' '))
            return
        }

        logger.trace { "Detected text command '${commandInfo.path}' with args '$args'" }

        rateLimitHandler.tryRun(commandInfo, event) { cancellableRateLimit ->
            if (!canRun(event, commandInfo)) {
                false
            } else {
                tryVariations(event, commandInfo, content, args, cancellableRateLimit)
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
        if (e is CancellationException)
            return logger.trace(e) { "Text command '$msg' was cancelled" }

        exceptionHandler.handleException(event, e, "text command '$msg'", mapOf("Message" to event.jumpUrl))
        if (e is InsufficientPermissionException) {
            replyError(event, messagesFactory.get(event).missingBotPermissions(event, setOf(e.permission)))
        } else {
            replyError(event, messagesFactory.get(event).uncaughtException(event))
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

    private fun getMsgNoPrefix(msg: String, channel: GuildMessageChannel): String? {
        return textCommandsContext.getEffectivePrefixes(channel)
            .find { prefix -> msg.startsWith(prefix) }
            ?.let { prefix -> msg.substring(prefix.length).trimStart() }
    }

    private suspend fun canRun(event: MessageReceivedEvent, commandInfo: TextCommandInfo): Boolean {
        val member = event.member ?: throwInternal("Text command was executed out of a Guild")
        val usability = commandInfo.getUsability(member, event.guildChannel)

        if (usability.isNotUsable) {
            val errorMessage = fromMessages(event) {
                when (usability.bestReason) {
                    UnusableReason.HIDDEN -> throwInternal("Hidden commands should have been ignored by ${TextCommandsListener::findCommandWithArgs.shortSignature}")
                    UnusableReason.OWNER_ONLY -> ownerOnly(event)
                    UnusableReason.USER_PERMISSIONS -> {
                        val missingPermissions = getMissingPermissions(commandInfo.userPermissions, member, event.guildChannel)
                        missingUserPermissions(event, missingPermissions)
                    }
                    UnusableReason.BOT_PERMISSIONS -> {
                        val missingPermissions = getMissingPermissions(commandInfo.botPermissions, event.guild.selfMember, event.guildChannel)
                        missingBotPermissions(event, missingPermissions)
                    }
                    UnusableReason.NSFW_ONLY -> nsfwOnly(event)
                }
            }

            replyError(event, errorMessage)
            return false
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
        val optionValues = variation.tryParseOptionValues(event, matchResult)
            ?: return ExecutionResult.CONTINUE //Go to next variation

        // At this point, we're sure that the command is executable
        checkFilters(globalFilters, variation.filters) { filter ->
            val rejectionReason = filter.checkSuspend(event, variation, args)
            if (rejectionReason != null) {
                logger.trace { "${filter.description} rejected text command '$content' by user ${event.author.id}: $rejectionReason" }
                return ExecutionResult.STOP
            }
        }

        variation.execute(event, optionValues)
        return ExecutionResult.OK
    }

    private suspend fun replyError(event: MessageReceivedEvent, message: MessageCreateData) {
        val channel = when {
            event.guildChannel.canTalk() -> event.channel
            else -> event.author.openPrivateChannel().await()
        }

        channel.sendMessage(message)
            .awaitCatching()
            .handle(ErrorResponse.CANNOT_SEND_TO_USER) {
                event.message.addReaction(textConfig.dmClosedEmoji).await()
            }
            .orThrow()
    }

    private suspend fun onCommandNotFound(event: MessageReceivedEvent, commandName: String) {
        if (!textConfig.showSuggestions) return

        val candidates = textCommandsContext.rootCommands
            .filter { it.getUsability(event.member!!, event.guildChannel).isVisible }

        val suggestions = suggestionSupplier.getSuggestions(commandName, candidates)
        if (suggestions.isNotEmpty()) {
            replyError(event, messagesFactory.get(event).commandNotFound(event, suggestions))
        }
    }

    private inline fun fromMessages(event: MessageReceivedEvent, crossinline block: TextCommandsMessages.() -> MessageCreateData): MessageCreateData {
        return messagesFactory.get(event).run(block)
    }

    internal enum class Status {
        /** Enabled */
        ENABLED,
        /** No ping as prefix, no prefix, has prefix supplier */
        USES_PREFIX_SUPPLIER,
        /** Has prefix supplier but no content intent, with ping-as-prefix (ok) */
        MISSING_CONTENT_INTENT_WITH_PREFIX_SUPPLIER_WITH_PING,
        /** Has prefix supplier but no content intent, without ping-as-prefix (error) */
        MISSING_CONTENT_INTENT_WITH_PREFIX_SUPPLIER_WITHOUT_PING,
        /** No prefix, with ping-as-prefix  */
        CAN_READ_PING,
        /** Uses prefix but no content intent, with ping (ok) */
        MISSING_CONTENT_INTENT_WITH_PREFIX_WITH_PING,
        /** Uses prefix but no content intent, without ping (error) */
        MISSING_CONTENT_INTENT_WITH_PREFIX_WITHOUT_PING,
        ;

        internal companion object {
            internal fun check(config: BTextConfig, jdaService: JDAService, textPrefixSupplier: TextPrefixSupplier?): Status {
                // Priority:
                // MISSING_CONTENT_INTENT_WITH_PREFIX_WITHOUT_PING
                // MISSING_CONTENT_INTENT_WITH_PREFIX_WITH_PING
                // MISSING_CONTENT_INTENT_WITH_PREFIX_SUPPLIER_WITHOUT_PING
                // MISSING_CONTENT_INTENT_WITH_PREFIX_SUPPLIER_WITH_PING
                // USES_PREFIX_SUPPLIER
                // CAN_READ_PING
                // ENABLED
                val hasContentIntent = GatewayIntent.MESSAGE_CONTENT in jdaService.intents
                val usePingAsPrefix = config.usePingAsPrefix
                val hasPrefix = config.prefixes.isNotEmpty()
                val hasPrefixSupplier = textPrefixSupplier != null

                return when {
                    hasPrefix && !hasContentIntent -> {
                        if (usePingAsPrefix) {
                            MISSING_CONTENT_INTENT_WITH_PREFIX_WITH_PING
                        } else {
                            MISSING_CONTENT_INTENT_WITH_PREFIX_WITHOUT_PING
                        }
                    }
                    hasPrefixSupplier && !hasContentIntent -> {
                        if (usePingAsPrefix) {
                            MISSING_CONTENT_INTENT_WITH_PREFIX_SUPPLIER_WITH_PING
                        } else {
                            MISSING_CONTENT_INTENT_WITH_PREFIX_SUPPLIER_WITHOUT_PING
                        }
                    }
                    !hasPrefix && !usePingAsPrefix && hasPrefixSupplier -> USES_PREFIX_SUPPLIER
                    !hasPrefix && usePingAsPrefix && !hasPrefixSupplier -> CAN_READ_PING
                    else -> ENABLED
                }
            }
        }
    }

    internal object ActivationCondition : ConditionalServiceChecker {
        // Require either message content or mention prefix
        override fun checkServiceAvailability(serviceContainer: ServiceContainer, checkedClass: Class<*>): String? {
            val config = serviceContainer.getServiceOrNull<BTextConfig>()
                ?: return "Text commands needs to be registered, see ${BTextConfig::class.simpleName}"

            fun prefixSupplierName() = serviceContainer.getService<TextPrefixSupplier>().javaClass.simpleNestedName
            return when (Status.check(config, serviceContainer.getService(), serviceContainer.getServiceOrNull())) {
                ENABLED -> null
                USES_PREFIX_SUPPLIER -> null
                MISSING_CONTENT_INTENT_WITH_PREFIX_SUPPLIER_WITH_PING -> null
                MISSING_CONTENT_INTENT_WITH_PREFIX_SUPPLIER_WITHOUT_PING ->
                    "Text prefixes supplied by ${prefixSupplierName()} can't be used without GatewayIntent.MESSAGE_CONTENT, and ${BTextConfig::usePingAsPrefix.reference} is disabled"
                CAN_READ_PING -> null
                MISSING_CONTENT_INTENT_WITH_PREFIX_WITH_PING -> null
                MISSING_CONTENT_INTENT_WITH_PREFIX_WITHOUT_PING ->
                    "Text prefixes can't be used without GatewayIntent.MESSAGE_CONTENT, and ${BTextConfig::usePingAsPrefix.reference} is disabled"
            }
        }
    }
}
