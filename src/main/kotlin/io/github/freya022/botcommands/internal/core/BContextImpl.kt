package io.github.freya022.botcommands.internal.core

import dev.minn.jda.ktx.events.CoroutineEventManager
import io.github.freya022.botcommands.api.BCInfo
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandsContext
import io.github.freya022.botcommands.api.commands.text.HelpBuilderConsumer
import io.github.freya022.botcommands.api.commands.text.TextCommandsContext
import io.github.freya022.botcommands.api.core.*
import io.github.freya022.botcommands.api.core.BContext.Status
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.config.putConfigInServices
import io.github.freya022.botcommands.api.core.events.BStatusChangeEvent
import io.github.freya022.botcommands.api.core.service.*
import io.github.freya022.botcommands.api.core.utils.logger
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandsContextImpl
import io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.AutocompleteInfoContainer
import io.github.freya022.botcommands.internal.commands.text.TextCommandsContextImpl
import io.github.freya022.botcommands.internal.core.service.ServiceContainerImpl
import io.github.freya022.botcommands.internal.core.service.StagingClassAnnotations
import io.github.freya022.botcommands.internal.core.service.condition.CustomConditionsContainer
import io.github.freya022.botcommands.internal.core.service.provider.ServiceProviders
import io.github.freya022.botcommands.internal.localization.DefaultDefaultMessagesSupplier
import io.github.freya022.botcommands.internal.utils.ReflectionMetadata
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.freya022.botcommands.internal.utils.unwrap
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.hooks.IEventManager
import net.dv8tion.jda.api.requests.ErrorResponse
import kotlin.reflect.KFunction
import kotlin.system.measureNanoTime
import kotlin.time.Duration.Companion.minutes

internal class BContextImpl internal constructor(override val config: BConfig, val eventManager: CoroutineEventManager) : BContext {
    private val logger = KotlinLogging.logger<BContext>()

    override val serviceContainer = ServiceContainerImpl(this) //Puts itself

    private var _stagingClassAnnotations: StagingClassAnnotations? = StagingClassAnnotations(config.serviceConfig)
    internal val stagingClassAnnotations: StagingClassAnnotations
        get() = _stagingClassAnnotations ?: throwInternal("Cannot use ServiceAnnotationsMap after it has been clearer")
    internal val serviceProviders = ServiceProviders()
    internal val customConditionsContainer = CustomConditionsContainer()

    init {
        serviceContainer.putService(this)
        serviceContainer.putServiceAs<BContext>(this)

        serviceContainer.putService(serviceContainer)
        serviceContainer.putServiceAs<ServiceContainer>(serviceContainer)

        serviceContainer.putService(eventManager)
        serviceContainer.putServiceAs<IEventManager>(eventManager) //Should be used if JDA is constructed as a service

        config.putConfigInServices(serviceContainer)
    }

    override val eventDispatcher: EventDispatcher by lazy { getService<EventDispatcher>() }

    override var status: Status = Status.PRE_LOAD
        private set

    override val defaultMessagesSupplier: DefaultMessagesSupplier by serviceContainer.lazyOrElse { DefaultDefaultMessagesSupplier(this) }

    override val settingsProvider: SettingsProvider? by serviceContainer.lazyOrNull()
    override val globalExceptionHandler: GlobalExceptionHandler? by serviceContainer.lazyOrNull()

    override val textCommandsContext = TextCommandsContextImpl()
    override val defaultEmbedSupplier: DefaultEmbedSupplier by serviceContainer.lazyOrElse { DefaultEmbedSupplier.Default() }
    override val defaultEmbedFooterIconSupplier: DefaultEmbedFooterIconSupplier by serviceContainer.lazyOrElse { DefaultEmbedFooterIconSupplier.Default() }
    override val helpBuilderConsumer: HelpBuilderConsumer? by serviceContainer.lazyOrNull()

    override val applicationCommandsContext = ApplicationCommandsContextImpl(this)

    init {
        serviceContainer.putServiceAs<TextCommandsContext>(textCommandsContext)
        serviceContainer.putServiceAs<ApplicationCommandsContext>(applicationCommandsContext)

        measureNanoTime {
            ReflectionMetadata.runScan(this)
        }.also { nano -> logger.trace { "Classes reflection took ${nano / 1000000.0} ms" } }
    }

    private val bcRegex = Regex("at ${Regex.escape("io.github.freya022.botcommands.")}(?:api|internal)[.a-z]*\\.(.+)")
    private var nextExceptionDispatch: Long = 0

    override fun dispatchException(message: String, t: Throwable?, extraContext: Map<String, Any?>) {
        if (config.disableExceptionsInDMs) return //Don't send DM exceptions in dev mode

        if (nextExceptionDispatch < System.currentTimeMillis()) {
            nextExceptionDispatch = System.currentTimeMillis() + 10.minutes.inWholeMilliseconds

            val content = buildString {
                appendLine(message)
                if (extraContext.isNotEmpty()) {
                    appendLine("## Context")
                    appendLine(extraContext.entries.joinToString("\n") { (name, value) -> "**$name:** $value" })
                }
                if (t != null) {
                    append("## Filtered exception\n```\n")
                    val stackTraceLines = t.unwrap().stackTraceToString()
                        .lineSequence()
                        .filterNot { "jdk.internal" in it }
                        .filterNot { "java.base/java.lang.reflect.Method" in it }
                        .filterNot { "kotlin.reflect.full" in it }
                        .filterNot { "kotlin.reflect.jvm.internal" in it }
                        .filterNot { "kotlin.coroutines.jvm.internal" in it }
                        .filterNot { "dev.reformator.stacktracedecoroutinator" in it }
                        .filterNot { "kotlinx.coroutines.DispatchedTask.run" in it }
                        .filterNot { "java.base/java.util.concurrent.Executors\$RunnableAdapter.call" in it }
                        .filterNot { "java.base/java.util.concurrent.FutureTask.run" in it }
                        .filterNot { "java.base/java.util.concurrent.ScheduledThreadPoolExecutor\$ScheduledFutureTask.run" in it }
                        .filterNot { "java.base/java.util.concurrent.ThreadPoolExecutor" in it }
                        .filterNot { "java.base/java.lang.Thread.run" in it }
                        //Remove lines without a source line number,
                        // they are usually generated methods like "invokeSuspend"
                        .filterNot { it.endsWith(".kt)") }
                        .filterNot { ".access$" in it }
                        .map { it.replace("    ", "\t") }
                        .map {
                            bcRegex.replace(it) { matchResult ->
                                val remaining = matchResult.groupValues[1]
                                "at BC-${BCInfo.VERSION}/$remaining"
                            }
                        }

                    for (stackTraceLine in stackTraceLines) {
                        if (this.length + stackTraceLine.length + 3 + 1 + 63 > Message.MAX_CONTENT_LENGTH) break
                        appendLine(stackTraceLine)
                    }
                    // Replace last newline with the code block end
                    replace(lastIndex, lastIndex + 1, "```")
                }
                append("\nPlease check the logs for more detail and possible exceptions")
            }

            jda.retrieveApplicationInfo()
                .map { applicationInfo -> applicationInfo.owner }
                .flatMap { user -> user.openPrivateChannel() }
                .flatMap { channel -> channel.sendMessage(content) }
                .queue(
                    null,
                    ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER) { logger.warn { "Could not send exception DM to owner" } }
                )
        }
    }

    override fun invalidateAutocompleteCache(autocompleteHandlerName: String) {
        getService<AutocompleteInfoContainer>()[autocompleteHandlerName]?.invalidate()
    }

    override fun invalidateAutocompleteCache(autocompleteHandler: KFunction<*>) {
        getService<AutocompleteInfoContainer>()[autocompleteHandler]?.invalidate()
    }

    internal fun clearStagingAnnotationsMap() {
        _stagingClassAnnotations = null
    }

    internal fun setStatus(newStatus: Status) {
        val oldStatus = this.status
        this.status = newStatus
        runBlocking { eventDispatcher.dispatchEvent(BStatusChangeEvent(this@BContextImpl, oldStatus, newStatus)) }
    }
}
