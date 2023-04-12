package com.freya02.botcommands.internal

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.BContext.Status
import com.freya02.botcommands.api.DefaultMessages
import com.freya02.botcommands.api.ExceptionHandler
import com.freya02.botcommands.api.SettingsProvider
import com.freya02.botcommands.api.commands.application.ApplicationCommandsContext
import com.freya02.botcommands.api.commands.prefixed.HelpBuilderConsumer
import com.freya02.botcommands.api.commands.prefixed.TextCommandsContext
import com.freya02.botcommands.api.core.DefaultMessagesSupplier
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
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.requests.ErrorResponse
import java.io.InputStream
import java.util.function.Supplier
import kotlin.time.Duration.Companion.minutes

class BContextImpl internal constructor(internal val config: BConfig, val eventManager: CoroutineEventManager) : BContext {
    private val logger = KotlinLogging.logger<BContext>()

    internal val classPathContainer: ClassPathContainer
    private val serviceContainer: ServiceContainer
    val eventDispatcher: EventDispatcher

    private var status : Status = Status.PRE_LOAD

    private var nextExceptionDispatch: Long = 0

    internal val isHelpDisabled: Boolean
        get() = config.textConfig.isHelpDisabled

    internal val textCommandsContext = TextCommandsContextImpl()

    private val applicationCommandsContext = ApplicationCommandsContextImpl(this)

    init {
        classPathContainer = ClassPathContainer(this)
        serviceContainer = ServiceContainer(this) //Puts itself, ctx, cem and cpc
        config.putConfigInServices(serviceContainer)
        serviceContainer.putServiceAs<ApplicationCommandsContext>(applicationCommandsContext)
        serviceContainer.putServiceAs<TextCommandsContext>(textCommandsContext)

        eventDispatcher = EventDispatcher(this, EventTreeService(this)) //Services put in ctor

        serviceContainer.preloadServices()
    }

    private val defaultMessagesSupplier: DefaultMessagesSupplier by lazy {
        (serviceContainer.getServiceOrNull<DefaultMessagesSupplier>() ?: DefaultDefaultMessagesSupplier).also {
            logger.info { "Using ${it::class.java.simpleName} as a ${DefaultMessagesSupplier::class.java.simpleName} instance" }
        }
    }

    override fun getServiceContainer(): ServiceContainer = serviceContainer

    inline fun <reified T : Any> getService() = getService(T::class)

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
        return defaultMessagesSupplier.get(locale)
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
