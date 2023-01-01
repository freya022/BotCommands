package com.freya02.botcommands.internal

import com.freya02.botcommands.api.*
import com.freya02.botcommands.api.BContext.Status
import com.freya02.botcommands.api.commands.prefixed.HelpBuilderConsumer
import com.freya02.botcommands.api.core.EventDispatcher
import com.freya02.botcommands.api.core.EventTreeService
import com.freya02.botcommands.api.core.ServiceContainer
import com.freya02.botcommands.api.core.config.BConfig
import com.freya02.botcommands.api.core.events.BStatusChangeEvent
import com.freya02.botcommands.internal.commands.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.commands.application.ApplicationCommandsContextImpl
import com.freya02.botcommands.internal.commands.application.autocomplete.AutocompleteHandlerContainer
import com.freya02.botcommands.internal.commands.application.slash.autocomplete.AutocompleteHandler
import com.freya02.botcommands.internal.commands.prefixed.TextCommandsContextImpl
import com.freya02.botcommands.internal.core.ClassPathContainer
import dev.minn.jda.ktx.events.CoroutineEventManager
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.ApplicationInfo
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.requests.ErrorResponse
import java.io.InputStream
import java.util.*
import java.util.function.Supplier
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.minutes

class BContextImpl(internal val config: BConfig, val eventManager: CoroutineEventManager) : BContext {
    private val logger = KotlinLogging.logger { }

    internal val classPathContainer: ClassPathContainer
    val serviceContainer: ServiceContainer
    val eventDispatcher: EventDispatcher

    private var status : Status = Status.PRE_LOAD

    //TODO replace by events
    private val registrationListeners: MutableList<RegistrationListener> = arrayListOf()

    private var nextExceptionDispatch: Long = 0

    val localizationManager = LocalizationManager()

    internal val isHelpDisabled: Boolean
        get() = config.textConfig.isHelpDisabled

    internal val textCommandsContext = TextCommandsContextImpl(this)

    private val applicationCommandsContext = ApplicationCommandsContextImpl(this)

    init {
        classPathContainer = ClassPathContainer(this)
        serviceContainer = ServiceContainer(this) //Puts itself, ctx, cem and cpc
        config.putConfigInServices(serviceContainer)

        eventDispatcher = EventDispatcher(this, EventTreeService(this)) //Services put in ctor

        serviceContainer.preloadServices()
    }

    inline fun <reified T : Any> getService() = getService(T::class)

    override fun <T : Any> getService(clazz: KClass<T>): T {
        return serviceContainer.getService(clazz)
    }

    override fun <T : Any> getService(clazz: Class<T>): T {
        return serviceContainer.getService(clazz)
    }

    override fun <T : Any> putService(service: T) {
        serviceContainer.putService(service)
    }

    override fun getJDA(): JDA {
        return serviceContainer.getService(JDA::class)
    }

    override fun getStatus(): Status = status

    fun setStatus(newStatus: Status) {
        runBlocking { eventDispatcher.dispatchEvent(BStatusChangeEvent(status, newStatus)) }
        this.status = newStatus
    }

    override fun getPrefixes(): List<String> = config.textConfig.prefixes

    override fun isPingAsPrefix(): Boolean = config.textConfig.usePingAsPrefix

    override fun getOwnerIds(): Collection<Long> {
        return config.ownerIds
    }

    override fun getDefaultMessages(locale: DiscordLocale): DefaultMessages {
        return config.defaultMessageProvider.apply(locale)
    }

    override fun getApplicationCommandsContext(): ApplicationCommandsContextImpl {
        return applicationCommandsContext
    }

    //TODO default method to get configs

    //TODO default method
    override fun getDefaultEmbedSupplier(): Supplier<EmbedBuilder> {
        return config.textConfig.defaultEmbedSupplier
    }

    //TODO default method
    override fun getDefaultFooterIconSupplier(): Supplier<InputStream?> {
        return config.textConfig.defaultFooterIconSupplier
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
        if (config.devMode) return //Don't send DM exceptions in dev mode

        if (nextExceptionDispatch < System.currentTimeMillis()) {
            nextExceptionDispatch = System.currentTimeMillis() + 10.minutes.inWholeMilliseconds

            val exceptionStr = if (t == null) "" else "\nException : \n%s".format(t.getDeepestCause())

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

    override fun getRegistrationListeners(): List<RegistrationListener> {
        return Collections.unmodifiableList(registrationListeners)
    }

    override fun addRegistrationListeners(vararg listeners: RegistrationListener) {
        registrationListeners += listeners
    }

    override fun getSettingsProvider(): SettingsProvider? { //TODO change to BConfig only, or default method in BContext ?
        if (!config.hasSettingsProvider()) return null
        return config.settingsProvider
    }

    override fun getHelpBuilderConsumer(): HelpBuilderConsumer? {
        return config.textConfig.helpBuilderConsumer
    }

    override fun getUncaughtExceptionHandler(): ExceptionHandler? = when {
        config.hasUncaughtExceptionHandler() -> config.uncaughtExceptionHandler
        else -> null
    }
}