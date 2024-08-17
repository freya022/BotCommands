package io.github.freya022.botcommands.internal.commands.text

import dev.minn.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.commands.Usability.UnusableReason
import io.github.freya022.botcommands.api.commands.ratelimit.CancellableRateLimit
import io.github.freya022.botcommands.api.commands.text.*
import io.github.freya022.botcommands.api.commands.text.annotations.RequiresTextCommands
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
import io.github.freya022.botcommands.api.core.utils.*
import io.github.freya022.botcommands.api.localization.DefaultMessagesFactory
import io.github.freya022.botcommands.internal.commands.ratelimit.withRateLimit
import io.github.freya022.botcommands.internal.commands.text.TextCommandsListener.Status.*
import io.github.freya022.botcommands.internal.core.ExceptionHandler
import io.github.freya022.botcommands.internal.localization.text.LocalizableTextCommandFactory
import io.github.freya022.botcommands.internal.utils.*
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.requests.GatewayIntent

private val logger = KotlinLogging.logger { }
private val spacePattern = Regex("\\s+")

@BService
@RequiresTextCommands
//TODO expand this to a custom condition, included in built-in text command stuff
@ConditionalService(TextCommandsListener.ActivationCondition::class)
internal class TextCommandsListener internal constructor(
    private val context: BContext,
    private val defaultMessagesFactory: DefaultMessagesFactory,
    private val textCommandsContext: TextCommandsContextImpl,
    private val localizableTextCommandFactory: LocalizableTextCommandFactory,
    filters: List<TextCommandFilter<Any>>,
    rejectionHandler: TextCommandRejectionHandler<Any>?,
    private val suggestionSupplier: TextSuggestionSupplier,
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

        // Could also check mentions, but this is way easier and faster
        val msg: String = suppressContentWarning { event.message.contentRaw }
        val content = getMsgNoPrefix(msg, event.guildChannel)
        if (content.isNullOrBlank()) return

        logger.trace { "Received text command: $msg" }

        scope.launchCatching({ handleException(event, it, msg) }) launch@{
            val member = event.member ?: throwInternal("Command caller member is null ! This shouldn't happen if the message isn't a webhook, or is the docs wrong ?")
            val isNotOwner = member !in context.botOwners

            val (commandInfo: TextCommandInfoImpl, args: String) = findCommandWithArgs(content, isNotOwner) ?: let {
                // At this point no top level command was found,
                // if a subcommand wasn't matched, it would simply appear in the args
                onCommandNotFound(event, content.substringBefore(' '))
                return@launch
            }

            logger.trace { "Detected text command '${commandInfo.path}' with args '$args'" }

            commandInfo.withRateLimit(context, event, isNotOwner) { cancellableRateLimit ->
                if (!canRun(event, commandInfo)) {
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
            replyError(event, defaultMessagesFactory.get(event).getBotPermErrorMsg(setOf(e.permission)))
        } else {
            replyError(event, defaultMessagesFactory.get(event).generalErrorMsg)
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
            val errorMessage: String = when (usability.bestReason) {
                UnusableReason.HIDDEN -> throwInternal("Hidden commands should have been ignored by ${TextCommandsListener::findCommandWithArgs.shortSignature}")
                UnusableReason.OWNER_ONLY -> defaultMessagesFactory.get(event).ownerOnlyErrorMsg
                UnusableReason.USER_PERMISSIONS -> {
                    val missingPermissions = getMissingPermissions(commandInfo.userPermissions, member, event.guildChannel)
                    defaultMessagesFactory.get(event).getUserPermErrorMsg(missingPermissions)
                }
                UnusableReason.BOT_PERMISSIONS -> {
                    val missingPermissions = getMissingPermissions(commandInfo.botPermissions, event.guild.selfMember, event.guildChannel)
                    defaultMessagesFactory.get(event).getBotPermErrorMsg(missingPermissions)
                }
                UnusableReason.NSFW_ONLY -> defaultMessagesFactory.get(event).nsfwOnlyErrorMsg
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

        channel.sendMessage(msg)
            .awaitCatching()
            .handle(ErrorResponse.CANNOT_SEND_TO_USER) {
                event.message.addReaction(context.textConfig.dmClosedEmoji).await()
            }
            .orThrow()
    }

    private suspend fun onCommandNotFound(event: MessageReceivedEvent, commandName: String) {
        if (!context.textConfig.showSuggestions) return

        val candidates = context.textCommandsContext.rootCommands
            .filter { it.getUsability(event.member!!, event.guildChannel).isVisible }

        val suggestions = suggestionSupplier.getSuggestions(commandName, candidates)
        if (suggestions.isNotEmpty()) {
            val suggestionsStr = suggestions.joinToString("**, **", "**", "**") { it.name }
            replyError(event, defaultMessagesFactory.get(event).getCommandNotFoundMsg(suggestionsStr))
        }
    }

    internal enum class Status {
        /** Disabled by config */
        DISABLED,
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
                // DISABLED
                // MISSING_CONTENT_INTENT_WITH_PREFIX_WITHOUT_PING
                // MISSING_CONTENT_INTENT_WITH_PREFIX_WITH_PING
                // MISSING_CONTENT_INTENT_WITH_PREFIX_SUPPLIER_WITHOUT_PING
                // MISSING_CONTENT_INTENT_WITH_PREFIX_SUPPLIER_WITH_PING
                // USES_PREFIX_SUPPLIER
                // CAN_READ_PING
                // ENABLED
                if (!config.enable) return DISABLED

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
            fun prefixSupplierName() = serviceContainer.getService<TextPrefixSupplier>().javaClass.simpleNestedName
            return when (Status.check(serviceContainer.getService(), serviceContainer.getService(), serviceContainer.getServiceOrNull())) {
                DISABLED -> "Text commands needs to be enabled, see ${BTextConfig::enable.reference}"
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