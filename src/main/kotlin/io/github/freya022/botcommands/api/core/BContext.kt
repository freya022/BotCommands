package io.github.freya022.botcommands.api.core

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandsContext
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.declaration.AutocompleteManager
import io.github.freya022.botcommands.api.commands.text.HelpBuilderConsumer
import io.github.freya022.botcommands.api.commands.text.TextCommandsContext
import io.github.freya022.botcommands.api.core.config.*
import io.github.freya022.botcommands.api.core.events.*
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.ServiceResult
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.localization.DefaultMessages
import io.github.freya022.botcommands.api.localization.DefaultMessagesFactory
import io.github.freya022.botcommands.internal.core.exceptions.ServiceException
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.Interaction
import kotlin.reflect.KFunction

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
    val debugConfig: BDebugConfig
        get() = config.debugConfig
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
     * Returns the IDs of the bot owners.
     *
     * @see BotOwners.ownerIds
     */
    @Deprecated(
        message = "Get from BotOwners directly",
        replaceWith = ReplaceWith("botOwners.ownerIds")
    )
    val ownerIds: Collection<Long> get() = botOwners.ownerIds

    /**
     * Returns the [SettingsProvider] service, or `null` if none exists.
     *
     * @see SettingsProvider
     */
    val settingsProvider: SettingsProvider?

    /**
     * Returns the [global exception handler][GlobalExceptionHandler],
     * used to handle errors caught by the framework, or `null` if none exists.
     *
     * @see GlobalExceptionHandler
     */
    val globalExceptionHandler: GlobalExceptionHandler?

    /**
     * Tells whether this user is an owner or not.
     *
     * @param userId ID of the user
     *
     * @return `true` if the user is an owner
     */
    @Deprecated(
        message = "Prefer using BotOwners#isOwner/contains",
        replaceWith = ReplaceWith(
            expression = "UserSnowflake.fromId(userId) in botOwners",
            imports = ["net.dv8tion.jda.api.entities.UserSnowflake"]
        )
    )
    fun isOwner(userId: Long): Boolean = UserSnowflake.fromId(userId) in botOwners

    /**
     * Returns the [DefaultMessages] instance for the provided Discord locale.
     *
     * @param locale The locale to get the messages in
     */
    @Deprecated(
        message = "Get from DefaultMessagesFactory",
        replaceWith = ReplaceWith(
            expression = "this.getService<DefaultMessagesFactory>().get(locale.toLocale())",
            imports = [
                "io.github.freya022.botcommands.api.localization.DefaultMessagesFactory",
                "io.github.freya022.botcommands.api.core.service.getService"
            ]
        )
    )
    fun getDefaultMessages(locale: DiscordLocale): DefaultMessages =
        getService<DefaultMessagesFactory>().get(locale.toLocale())

    /**
     * Returns the [DefaultMessages] instance for this Guild's locale
     *
     * @param guild The Guild to take the locale from
     */
    @Deprecated(
        message = "Get from DefaultMessagesFactory, using the message event",
        replaceWith = ReplaceWith(
            expression = "this.getService<DefaultMessagesFactory>().get(event)",
            imports = [
                "io.github.freya022.botcommands.api.localization.DefaultMessagesFactory",
                "io.github.freya022.botcommands.api.core.service.getService"
            ]
        )
    )
    fun getDefaultMessages(guild: Guild?): DefaultMessages {
        return getService<DefaultMessagesFactory>().get(getEffectiveLocale(guild).toLocale())
    }

    /**
     * Returns the [DefaultMessages] instance for this user's locale
     *
     * @param interaction The Interaction to take the user's locale from
     */
    @Deprecated(
        message = "Get from DefaultMessagesFactory",
        replaceWith = ReplaceWith(
            expression = "this.getService<DefaultMessagesFactory>().get(interaction)",
            imports = [
                "io.github.freya022.botcommands.api.localization.DefaultMessagesFactory",
                "io.github.freya022.botcommands.api.core.service.getService"
            ]
        )
    )
    fun getDefaultMessages(interaction: Interaction): DefaultMessages {
        return getService<DefaultMessagesFactory>().get(interaction)
    }

    /**
     * Sends an exception message to the unique bot owner, retrieved via [JDA.retrieveApplicationInfo]
     *
     * @param message The message describing the context
     * @param t       An optional exception
     */
    fun dispatchException(message: String, t: Throwable?) = dispatchException(message, t, emptyMap())

    /**
     * Sends an exception message to the unique bot owner, retrieved via [JDA.retrieveApplicationInfo]
     *
     * @param message      The message describing the context
     * @param t            An optional exception
     * @param extraContext Additional context of the exception; can be empty
     */
    fun dispatchException(message: String, t: Throwable?, extraContext: Map<String, Any?>)

    /**
     * Returns the [DiscordLocale] for the specified [Guild]
     *
     * @param guild The [Guild] in which to take the [DiscordLocale] from
     *
     * @return The [DiscordLocale] of the [Guild]
     */
    @Deprecated("Replaced with TextCommandLocaleProvider")
    fun getEffectiveLocale(guild: Guild?): DiscordLocale {
        if (guild != null && guild.features.contains("COMMUNITY")) {
            return guild.locale
        }

        return settingsProvider?.getLocale(guild)
            //Discord default
            ?: return DiscordLocale.ENGLISH_US
    }

    //region Text commands
    /**
     * Returns the [TextCommandsContext] service.
     *
     * @see TextCommandsContext
     */
    val textCommandsContext: TextCommandsContext

    /**
     * Returns the full list of prefixes used to trigger the bot.
     *
     * This does not include ping-as-prefix.
     *
     * @return Full list of prefixes
     */
    @Deprecated("Moved to TextCommandsContext", ReplaceWith("textCommandsContext.prefixes"))
    val prefixes: List<String>
        get() = textCommandsContext.prefixes

    /**
     * @return `true` if the bot responds to its own mention.
     */
    @Deprecated("Moved to TextCommandsContext", ReplaceWith("textCommandsContext.isPingAsPrefix"))
    val isPingAsPrefix: Boolean
        get() = textCommandsContext.isPingAsPrefix

    /**
     * Returns the preferred prefix for triggering this bot,
     * or `null` if [BTextConfig.usePingAsPrefix] is disabled and no prefix was added in [BTextConfig.prefixes].
     *
     * @return The preferred prefix
     */
    @Deprecated("Moved to TextCommandsContext", ReplaceWith("textCommandsContext.getPreferredPrefix(jda)"))
    val prefix: String?
        get() = textCommandsContext.getPreferredPrefix(jda)

    /**
     * Returns the [DefaultEmbedSupplier] service.
     *
     * @see DefaultEmbedSupplier
     */
    @Deprecated("Moved to TextCommandsContext", ReplaceWith("textCommandsContext.defaultEmbedSupplier"))
    val defaultEmbedSupplier: DefaultEmbedSupplier
        get() = textCommandsContext.defaultEmbedSupplier

    /**
     * Returns the [DefaultEmbedFooterIconSupplier] service.
     *
     * @see DefaultEmbedFooterIconSupplier
     */
    @Deprecated("Moved to TextCommandsContext", ReplaceWith("textCommandsContext.defaultEmbedFooterIconSupplier"))
    val defaultEmbedFooterIconSupplier: DefaultEmbedFooterIconSupplier
        get() = textCommandsContext.defaultEmbedFooterIconSupplier

    /**
     * Returns the help builder consumer - changes the EmbedBuilder given to add more stuff in it
     *
     * @return The help builder consumer
     */
    @Deprecated("Moved to TextCommandsContext", ReplaceWith("textCommandsContext.helpBuilderConsumer"))
    val helpBuilderConsumer: HelpBuilderConsumer?
        get() = textCommandsContext.helpBuilderConsumer
    //endregion

    //region Application commands
    /**
     * Returns the application commands context, this is for user/message/slash commands and related methods
     *
     * @return The [ApplicationCommandsContext] object
     */
    val applicationCommandsContext: ApplicationCommandsContext

    /**
     * Invalidates the autocomplete cache of the specified autocomplete handler.
     *
     * This means that the cache of this autocomplete handler will be fully cleared.
     *
     * @param autocompleteHandlerName The name of the autocomplete handler,
     * supplied at [AutocompleteHandler.name] or [AutocompleteManager.autocomplete]
     */
    fun invalidateAutocompleteCache(autocompleteHandlerName: String)

    /**
     * Invalidates the autocomplete cache of the specified autocomplete handler.
     *
     * This means that the cache of this autocomplete handler will be fully cleared.
     *
     * @param autocompleteHandler The autocomplete handler, supplied at [AutocompleteManager.autocomplete]
     */
    @JvmSynthetic
    fun invalidateAutocompleteCache(autocompleteHandler: KFunction<*>)
    //endregion
}
