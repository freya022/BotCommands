package io.github.freya022.botcommands.api.core

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandsContext
import io.github.freya022.botcommands.api.core.config.*
import io.github.freya022.botcommands.api.core.events.*
import io.github.freya022.botcommands.api.core.hooks.EventDispatcher
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.ServiceResult
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.internal.core.exceptions.ServiceException
import net.dv8tion.jda.api.JDA
import java.time.Duration as JavaDuration
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.toKotlinDuration

/**
 * Main context for BotCommands framework.
 */
@InterfacedService(acceptMultiple = false)
interface BContext {
    /**
     * Initialization status of the framework.
     *
     * Each status change fires a [BStatusChangeEvent].
     */
    enum class Status {
        /**
         * Fires [PreLoadEvent].
         */
        PRE_LOAD,

        /**
         * Fires [LoadEvent].
         */
        LOAD,

        /**
         * Fires [PostLoadEvent].
         */
        POST_LOAD,

        /**
         * State at which point all services are loaded.
         *
         * Fires [BReadyEvent].
         */
        READY,

        /**
         * The shutdown sequence has been initiated.
         *
         * A [BStatusChangeEvent] if fired, but no dedicated event.
         */
        SHUTTING_DOWN,

        /**
         * The instance has been completely shutdown alongside the underlying JDA instance(s)
         *
         * Fires [BShutdownEvent].
         */
        SHUTDOWN,
    }

    //region Configs
    val config: BConfig
    val eventManagerConfig: BEventManagerConfig
        get() = config.eventManagerConfig
    val localizationConfig: BLocalizationConfig
        get() = config.localizationConfig
    val applicationConfig: BApplicationConfig
        get() = config.applicationConfig
    val coroutineScopesConfig: BCoroutineScopesConfig
        get() = config.coroutineScopesConfig
    val serviceConfig: BServiceConfig
        get() = config.serviceConfig
    //endregion

    //region Services
    /**
     * Returns the [ServiceContainer] service.
     *
     * @see ServiceContainer
     */
    val serviceContainer: ServiceContainer

    fun <T : Any> tryGetService(clazz: Class<T>): ServiceResult<T> = serviceContainer.tryGetService(clazz)

    fun <T : Any> getService(clazz: Class<T>): T = serviceContainer.getService(clazz)
    //endregion

    /**
     * Returns the [EventDispatcher] service.
     *
     * @see EventDispatcher
     */
    val eventDispatcher: EventDispatcher

    /**
     * Returns the JDA instance associated with this context.
     *
     * **Note:** This must not be used to access JDA before it has started,
     * prefer using [InjectedJDAEvent].
     *
     * @throws ServiceException If JDA is not registered yet
     */
    val jda: JDA get() = getService<JDA>()

    /**
     * Returns the status of the framework.
     *
     * @see Status
     */
    val status: Status

    /**
     * Returns the [BotOwners] service.
     *
     * @see BotOwners
     */
    val botOwners: BotOwners

    /**
     * Returns the [global exception handler][GlobalExceptionHandler],
     * used to handle errors caught by the framework, or `null` if none exists.
     *
     * @see GlobalExceptionHandler
     */
    val globalExceptionHandler: GlobalExceptionHandler?

    /**
     * Sends an exception message to the [bot owners][BotOwners].
     *
     * @param message The message describing the context
     * @param t       An optional exception
     */
    fun dispatchException(message: String, t: Throwable?) = dispatchException(message, t, emptyMap())

    /**
     * Sends an exception message to the [bot owners][BotOwners].
     *
     * @param message      The message describing the context
     * @param t            An optional exception
     * @param extraContext Additional context of the exception; can be empty
     */
    fun dispatchException(message: String, t: Throwable?, extraContext: Map<String, Any?>)

    /**
     * Gets the message that would be sent by [dispatchException].
     *
     * @param message      The message describing the context
     * @param t            An optional exception
     * @param extraContext Additional context of the exception; can be empty
     */
    fun getExceptionContent(message: String, t: Throwable?, extraContext: Map<String, Any?>): String

    /**
     * Shuts down this instance of BotCommands, shutting down JDA in the process, then shutting down all executors.
     *
     * All currently running tasks will not be interrupted, unless [shutdownNow] is called.
     *
     * @see JDA.shutdown
     * @see shutdownNow
     */
    fun shutdown()

    /**
     * Immediately shuts down this instance of BotCommands, shutting down JDA in the process, then shutting down all executors.
     *
     * All currently running tasks will be interrupted, and all coroutine scopes are canceled.
     *
     * @see JDA.shutdownNow
     */
    fun shutdownNow()

    /**
     * Blocks the current thread until the [status] is set to [SHUTDOWN][BContext.Status.SHUTDOWN].
     *
     * This will wait indefinitely, specify a duration if you want a timeout.
     *
     * Unless [shutdownNow] has been used, the shutdown time depends on the amount of requests queued in JDA,
     * and the amount of tasks submitted to the various executors.
     *
     * @return Always `true`
     */
    fun awaitShutdown(): Boolean = awaitShutdown(Duration.INFINITE)

    /**
     * Blocks the current thread until the [status] is set to [SHUTDOWN][BContext.Status.SHUTDOWN],
     * or until the timeout has been reached.
     *
     * Unless [shutdownNow] has been used, the shutdown time depends on the amount of requests queued in JDA,
     * and the amount of tasks submitted to the various executors.
     *
     * @return `true` if the shutdown finished before the timeout, `false` if the timeout was reached
     */
    fun awaitShutdown(timeout: JavaDuration): Boolean = awaitShutdown(timeout.toKotlinDuration())

    /**
     * Blocks the current thread until the [status] is set to [SHUTDOWN][BContext.Status.SHUTDOWN],
     * or until the timeout has been reached.
     *
     * Unless [shutdownNow] has been used, the shutdown time depends on the amount of requests queued in JDA,
     * and the amount of tasks submitted to the various executors.
     *
     * @return `true` if the shutdown finished before the timeout, `false` if the timeout was reached
     */
    fun awaitShutdown(timeout: Duration): Boolean

    /**
     * Blocks the current thread until the [status] is set to [SHUTDOWN][BContext.Status.SHUTDOWN],
     * or until the timeout has been reached.
     *
     * Unless [shutdownNow] has been used, the shutdown time depends on the amount of requests queued in JDA,
     * and the amount of tasks submitted to the various executors.
     *
     * @return `true` if the shutdown finished before the timeout, `false` if the timeout was reached
     */
    fun awaitShutdown(timeout: Long, unit: TimeUnit): Boolean =
        awaitShutdown(JavaDuration.of(timeout, unit.toChronoUnit()))

    /**
     * Returns the application commands context, this is for user/message/slash commands and related methods
     *
     * @return The [ApplicationCommandsContext] object
     */
    val applicationCommandsContext: ApplicationCommandsContext
}
