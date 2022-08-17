package com.freya02.botcommands.internal

import com.freya02.botcommands.api.*
import com.freya02.botcommands.api.components.ComponentManager
import com.freya02.botcommands.api.prefixed.HelpBuilderConsumer
import com.freya02.botcommands.core.api.config.BConfig
import com.freya02.botcommands.core.internal.ClassPathContainer
import com.freya02.botcommands.core.internal.EventDispatcher
import com.freya02.botcommands.core.internal.ServiceContainer
import com.freya02.botcommands.internal.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.application.ApplicationCommandsContextImpl
import com.freya02.botcommands.internal.application.slash.autocomplete.AutocompleteHandler
import com.freya02.botcommands.internal.prefixed.TextCommandsContextImpl
import dev.minn.jda.ktx.events.CoroutineEventManager
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.ApplicationInfo
import net.dv8tion.jda.api.entities.PrivateChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.requests.ErrorResponse
import java.io.InputStream
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.function.Supplier
import kotlin.reflect.KClass

class BContextImpl(private val config: BConfig, val eventManager: CoroutineEventManager) : BContext {
    private val logger = Logging.getLogger()

    internal val classPathContainer: ClassPathContainer
    val serviceContainer: ServiceContainer
    val eventDispatcher: EventDispatcher

    //TODO replace by events
    private val registrationListeners: MutableList<RegistrationListener> = arrayListOf()

    private var nextExceptionDispatch: Long = 0

    private var uncaughtExceptionHandler: ExceptionHandler? = null

    val localizationManager = LocalizationManager()

    internal val isHelpDisabled: Boolean
        get() = config.textConfig.isHelpDisabled

    internal val textCommandsContext = TextCommandsContextImpl(this)

    private val applicationCommandsContext = ApplicationCommandsContextImpl(this)

    init {
        classPathContainer = ClassPathContainer(this)
        serviceContainer = ServiceContainer(this) //Puts itself, ctx, cem and cpc
        eventDispatcher = EventDispatcher(this) //Service put in ctor

        config.putConfigInServices(serviceContainer)
        serviceContainer.preloadServices()
    }

    override fun <T : Any> getService(clazz: KClass<T>): T {
        return serviceContainer.getService(clazz)
    }

    override fun <T : Any> getService(clazz: Class<T>): T {
        return serviceContainer.getService(clazz)
    }

    override fun getConfig(): BConfig = config

    override fun getJDA(): JDA {
        return serviceContainer.getService(JDA::class)
    }

    override fun getPrefixes(): List<String> {
        return config.textConfig.prefixes
    }

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

    internal fun getAutocompleteHandler(autocompleteHandlerName: String): AutocompleteHandler? {
        TODO()
    }

    override fun invalidateAutocompleteCache(autocompleteHandlerName: String) {
//        val handler = getAutocompleteHandler(autocompleteHandlerName)
//            ?: throwUser("Autocomplete handler name not found for '$autocompleteHandlerName'")
//        handler.invalidate()
        TODO()
    }

    val applicationCommandsView: Collection<ApplicationCommandInfo>
        get() = getApplicationCommandsContext()
            .mutableApplicationCommandMap
            .allApplicationCommands

    override fun dispatchException(message: String, t: Throwable?) {
        if (nextExceptionDispatch < System.currentTimeMillis()) {
            nextExceptionDispatch = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10)

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

    override fun getComponentManager(): ComponentManager {
        return serviceContainer.getService(config.componentsConfig.componentManagerStrategy)
    }

    override fun getSettingsProvider(): SettingsProvider? { //TODO change to BConfig only, or default method in BContext ?
        if (!config.hasSettingsProvider()) return null
        return config.settingsProvider
    }

    override fun getHelpBuilderConsumer(): HelpBuilderConsumer? {
        return config.textConfig.helpBuilderConsumer
    }

    fun setUncaughtExceptionHandler(exceptionHandler: ExceptionHandler?) {
        uncaughtExceptionHandler = exceptionHandler
    }

    override fun getUncaughtExceptionHandler(): ExceptionHandler? {
        return uncaughtExceptionHandler
    }
}