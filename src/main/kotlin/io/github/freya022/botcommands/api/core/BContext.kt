package io.github.freya022.botcommands.api.core

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandsContext
import io.github.freya022.botcommands.api.commands.text.TextCommandsContext
import io.github.freya022.botcommands.api.core.config.*
import io.github.freya022.botcommands.api.core.events.*
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.ServiceResult
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.internal.core.exceptions.ServiceException
import net.dv8tion.jda.api.JDA

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
        READY
    }

    //region Configs
    val config: BConfig
    val localizationConfig: BLocalizationConfig
        get() = config.localizationConfig
    val applicationConfig: BApplicationConfig
        get() = config.applicationConfig
    val componentsConfig: BComponentsConfig
        get() = config.componentsConfig
    val coroutineScopesConfig: BCoroutineScopesConfig
        get() = config.coroutineScopesConfig
    val serviceConfig: BServiceConfig
        get() = config.serviceConfig
    val textConfig: BTextConfig
        get() = config.textConfig
    //endregion

    //region Services
    /**
     * Returns the [ServiceContainer] service.
     *
     * @see ServiceContainer
     */
    val serviceContainer: ServiceContainer

    //TODO docs
    fun <T : Any> tryGetService(clazz: Class<T>): ServiceResult<T> = serviceContainer.tryGetService(clazz)

    //TODO docs
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
     * Returns the initialization status of the framework.
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
     * Returns the [TextCommandsContext] service.
     *
     * @see TextCommandsContext
     */
    val textCommandsContext: TextCommandsContext

    /**
     * Returns the application commands context, this is for user/message/slash commands and related methods
     *
     * @return The [ApplicationCommandsContext] object
     */
    val applicationCommandsContext: ApplicationCommandsContext
}
