package com.freya02.botcommands.internal

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.BContext.Status
import com.freya02.botcommands.api.DefaultMessages
import com.freya02.botcommands.api.commands.application.ApplicationCommandsContext
import com.freya02.botcommands.api.commands.prefixed.HelpBuilderConsumer
import com.freya02.botcommands.api.commands.prefixed.TextCommandsContext
import com.freya02.botcommands.api.core.*
import com.freya02.botcommands.api.core.config.BConfig
import com.freya02.botcommands.api.core.config.putConfigInServices
import com.freya02.botcommands.api.core.events.BStatusChangeEvent
import com.freya02.botcommands.api.core.service.ServiceStart
import com.freya02.botcommands.api.core.service.annotations.InjectedService
import com.freya02.botcommands.api.core.service.putServiceAs
import com.freya02.botcommands.internal.commands.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.commands.application.ApplicationCommandsContextImpl
import com.freya02.botcommands.internal.commands.application.autocomplete.AutocompleteHandlerContainer
import com.freya02.botcommands.internal.commands.application.slash.autocomplete.AutocompleteHandler
import com.freya02.botcommands.internal.commands.prefixed.TextCommandsContextImpl
import com.freya02.botcommands.internal.core.service.*
import dev.minn.jda.ktx.events.CoroutineEventManager
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.ApplicationInfo
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.hooks.IEventManager
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.requests.ErrorResponse
import kotlin.time.Duration.Companion.minutes

@InjectedService
class BContextImpl internal constructor(private val config: BConfig, val eventManager: CoroutineEventManager) : BContext {
    private val logger = KotlinLogging.logger<BContext>()

    private val serviceContainer: ServiceContainerImpl
    internal val serviceAnnotationsMap = ServiceAnnotationsMap(config.serviceConfig)
    internal val instantiableServiceAnnotationsMap get() = getService<InstantiableServiceAnnotationsMap>()
    internal val serviceProviders = ServiceProviders()
    val eventDispatcher: EventDispatcher get() = getService<EventDispatcher>()

    private var status : Status = Status.PRE_LOAD

    private var nextExceptionDispatch: Long = 0

    internal val isHelpDisabled: Boolean
        get() = config.textConfig.isHelpDisabled

    internal val textCommandsContext = TextCommandsContextImpl()

    private val applicationCommandsContext = ApplicationCommandsContextImpl(this)

    init {
        serviceContainer = ServiceContainerImpl(this) //Puts itself

        serviceContainer.putService(this)
        serviceContainer.putServiceAs<BContext>(this)

        serviceContainer.putService(eventManager)
        serviceContainer.putServiceAs<IEventManager>(eventManager) //Should be used if JDA is constructed as a service

        config.putConfigInServices(serviceContainer)

        serviceContainer.putServiceAs<ApplicationCommandsContext>(applicationCommandsContext)
        serviceContainer.putServiceAs<TextCommandsContext>(textCommandsContext)

        serviceContainer.loadServices(this.loadableServices, ServiceStart.DEFAULT)
    }

    private val _defaultMessagesSupplier by serviceContainer.interfacedService<DefaultMessagesSupplier, _> { DefaultDefaultMessagesSupplier }
    private val _settingsProvider by serviceContainer.nullableInterfacedService<SettingsProvider>()
    private val _globalExceptionHandler by serviceContainer.nullableInterfacedService<GlobalExceptionHandler>()
    private val _defaultEmbedSupplier by serviceContainer.interfacedService<DefaultEmbedSupplier, _> { DefaultEmbedSupplier.Default() }
    private val _defaultEmbedFooterIconSupplier by serviceContainer.interfacedService<DefaultEmbedFooterIconSupplier, _> { DefaultEmbedFooterIconSupplier.Default() }
    private val _helpBuilderConsumer by serviceContainer.nullableInterfacedService<HelpBuilderConsumer>()

    override fun getConfig() = config

    override fun getServiceContainer(): ServiceContainerImpl = serviceContainer

    inline fun <reified T : Any> getService() = getService(T::class)

    override fun getJDA(): JDA {
        return serviceContainer.getService(JDA::class)
    }

    override fun getStatus(): Status = status

    fun setStatus(newStatus: Status) {
        this.status = newStatus
        runBlocking { eventDispatcher.dispatchEvent(BStatusChangeEvent(status, newStatus)) }
    }

    override fun getPrefixes(): List<String> = config.textConfig.prefixes

    override fun isPingAsPrefix(): Boolean = config.textConfig.usePingAsPrefix

    override fun getOwnerIds(): Collection<Long> {
        return config.ownerIds
    }

    override fun getDefaultMessages(locale: DiscordLocale): DefaultMessages {
        return _defaultMessagesSupplier.get(locale)
    }

    override fun getApplicationCommandsContext(): ApplicationCommandsContextImpl {
        return applicationCommandsContext
    }

    override fun getDefaultEmbedSupplier(): DefaultEmbedSupplier {
        return _defaultEmbedSupplier
    }

    override fun getDefaultFooterIconSupplier(): DefaultEmbedFooterIconSupplier {
        return _defaultEmbedFooterIconSupplier
    }

    private fun getAutocompleteHandler(autocompleteHandlerName: String): AutocompleteHandler? {
        return getService<AutocompleteHandlerContainer>()[autocompleteHandlerName]
    }

    override fun invalidateAutocompleteCache(autocompleteHandlerName: String) {
        getAutocompleteHandler(autocompleteHandlerName)?.invalidate()
    }

    val applicationCommandsView: Collection<ApplicationCommandInfo>
        get() = getApplicationCommandsContext()
            .mutableApplicationCommandMap
            .allApplicationCommands

    override fun dispatchException(message: String, t: Throwable?) {
        if (config.disableExceptionsInDMs) return //Don't send DM exceptions in dev mode

        if (nextExceptionDispatch < System.currentTimeMillis()) {
            nextExceptionDispatch = System.currentTimeMillis() + 10.minutes.inWholeMilliseconds

            val exceptionStr = when (t) {
                null -> ""
                else -> "\nException:```\n${
                    t.unreflect().stackTraceToString()
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

    override fun getSettingsProvider(): SettingsProvider? {
        return _settingsProvider
    }

    override fun getHelpBuilderConsumer(): HelpBuilderConsumer? {
        return _helpBuilderConsumer
    }

    override fun getGlobalExceptionHandler(): GlobalExceptionHandler? {
        return _globalExceptionHandler
    }
}
