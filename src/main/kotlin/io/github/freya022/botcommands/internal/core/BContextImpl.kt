package io.github.freya022.botcommands.internal.core

import dev.minn.jda.ktx.events.CoroutineEventManager
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandsContext
import io.github.freya022.botcommands.api.commands.prefixed.HelpBuilderConsumer
import io.github.freya022.botcommands.api.commands.prefixed.TextCommandsContext
import io.github.freya022.botcommands.api.core.*
import io.github.freya022.botcommands.api.core.BContext.Status
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.config.putConfigInServices
import io.github.freya022.botcommands.api.core.events.BStatusChangeEvent
import io.github.freya022.botcommands.api.core.service.*
import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.api.core.utils.logger
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandsContextImpl
import io.github.freya022.botcommands.internal.commands.application.autocomplete.AutocompleteHandlerContainer
import io.github.freya022.botcommands.internal.commands.prefixed.TextCommandsContextImpl
import io.github.freya022.botcommands.internal.core.service.*
import io.github.freya022.botcommands.internal.localization.DefaultDefaultMessagesSupplier
import io.github.freya022.botcommands.internal.utils.ReflectionMetadata
import io.github.freya022.botcommands.internal.utils.unwrap
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.ApplicationInfo
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.hooks.IEventManager
import net.dv8tion.jda.api.requests.ErrorResponse
import kotlin.system.measureNanoTime
import kotlin.time.Duration.Companion.minutes

//TODO internal
@InjectedService
class BContextImpl internal constructor(override val config: BConfig, val eventManager: CoroutineEventManager) : BContext {
    private val logger = KotlinLogging.logger<BContext>()

    override val serviceContainer: ServiceContainerImpl = ServiceContainerImpl(this) //Puts itself

    internal val serviceAnnotationsMap = ServiceAnnotationsMap(config.serviceConfig)
    internal val instantiableServiceAnnotationsMap get() = getService<InstantiableServiceAnnotationsMap>()
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

    private var nextExceptionDispatch: Long = 0

    override fun dispatchException(message: String, t: Throwable?) {
        if (config.disableExceptionsInDMs) return //Don't send DM exceptions in dev mode

        if (nextExceptionDispatch < System.currentTimeMillis()) {
            nextExceptionDispatch = System.currentTimeMillis() + 10.minutes.inWholeMilliseconds

            val exceptionStr = when (t) {
                null -> ""
                else -> "\nException:```\n${
                    t.unwrap().stackTraceToString()
                        .lineSequence()
                        .filterNot { "jdk.internal" in it }
                        .filterNot { "kotlin.reflect.jvm.internal" in it }
                        .filterNot { "kotlin.coroutines.jvm.internal" in it }
                        .map { it.replace("    ", "\t") }
                        .fold("") { acc, s ->
                            when {
                                acc.length + s.length <= Message.MAX_CONTENT_LENGTH - 256 -> acc + s + "\n"
                                else -> acc
                            }
                        }.trimEnd()
                }```"
            }

            jda.retrieveApplicationInfo()
                .map { obj: ApplicationInfo -> obj.owner }
                .flatMap { obj: User -> obj.openPrivateChannel() }
                .flatMap { channel: PrivateChannel ->
                    channel.sendMessage("$message$exceptionStr\n\nPlease check the logs for more detail and possible exceptions")
                }
                .queue(
                    null,
                    ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER) { logger.warn("Could not send exception DM to owner") }
                )
        }
    }

    override fun invalidateAutocompleteCache(autocompleteHandlerName: String) {
        getService<AutocompleteHandlerContainer>()[autocompleteHandlerName]?.invalidate()
    }

    internal fun setStatus(newStatus: Status) {
        val oldStatus = this.status
        this.status = newStatus
        runBlocking { eventDispatcher.dispatchEvent(BStatusChangeEvent(oldStatus, newStatus)) }
    }
}
