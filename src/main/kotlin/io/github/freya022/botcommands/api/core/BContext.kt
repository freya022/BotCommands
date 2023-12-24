package io.github.freya022.botcommands.api.core

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandsContext
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler
import io.github.freya022.botcommands.api.commands.text.HelpBuilderConsumer
import io.github.freya022.botcommands.api.commands.text.TextCommandsContext
import io.github.freya022.botcommands.api.core.config.*
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.ServiceResult
import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.localization.DefaultMessages
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.Interaction

@InjectedService
interface BContext {
    enum class Status {
        PRE_LOAD,
        LOAD,
        POST_LOAD,
        READY
    }

    //region Configs
    val config: BConfig
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
    //TODO docs
    val serviceContainer: ServiceContainer

    //TODO docs
    fun <T : Any> tryGetService(clazz: Class<T>): ServiceResult<T> = serviceContainer.tryGetService(clazz)

    //TODO docs
    fun <T : Any> getService(clazz: Class<T>): T = serviceContainer.getService(clazz)

    //TODO docs
    fun putService(service: Any): Unit = serviceContainer.putService(service)
    //endregion

    //TODO docs
    val eventDispatcher: EventDispatcher

    /**
     * Returns the JDA instance associated with this context
     *
     * @return the JDA instance of this context
     */
    val jda: JDA get() = getService<JDA>()

    //TODO docs
    val status: Status

    /**
     * Returns the IDs of the bot owners
     *
     * @return the IDs of the bot owners
     */
    val ownerIds: Collection<Long> get() = config.ownerIds

    val defaultMessagesSupplier: DefaultMessagesSupplier

    /**
     * Returns the [SettingsProvider] for this context
     *
     * @return The current [SettingsProvider]
     */
    val settingsProvider: SettingsProvider?

    /**
     * Returns the [global exception handler][GlobalExceptionHandler], used to handle errors caught by the framework.
     *
     * @return The global exception handler
     * @see GlobalExceptionHandler
     */
    val globalExceptionHandler: GlobalExceptionHandler?

    /**
     * Tells whether this user is an owner or not.
     *
     * @param userId ID of the user
     * @return `true` if the user is an owner
     */
    fun isOwner(userId: Long): Boolean = userId in ownerIds

    fun getDefaultMessages(locale: DiscordLocale): DefaultMessages = defaultMessagesSupplier.get(locale)

    /**
     * Returns the [DefaultMessages] instance for this Guild's locale
     *
     * @param guild The Guild to take the locale from
     *
     * @return The [DefaultMessages] instance with the Guild's locale
     */
    fun getDefaultMessages(guild: Guild?): DefaultMessages {
        return getDefaultMessages(getEffectiveLocale(guild))
    }

    /**
     * Returns the [DefaultMessages] instance for this user's locale
     *
     * @param interaction The Interaction to take the user's locale from
     *
     * @return The [DefaultMessages] instance with the user's locale
     */
    fun getDefaultMessages(interaction: Interaction): DefaultMessages {
        return getDefaultMessages(interaction.userLocale)
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
    fun getEffectiveLocale(guild: Guild?): DiscordLocale {
        if (guild != null && guild.features.contains("COMMUNITY")) {
            return guild.locale
        }

        return settingsProvider?.getLocale(guild)
            //Discord default
            ?: return DiscordLocale.ENGLISH_US
    }

    //region Text commands
    //TODO docs
    val textCommandsContext: TextCommandsContext

    /**
     * Returns the full list of prefixes used to trigger the bot.
     *
     * This does not include ping-as-prefix.
     *
     * @return Full list of prefixes
     */
    val prefixes: List<String> get() = textConfig.prefixes

    /**
     * @return `true` if the bot responds to its own mention.
     */
    val isPingAsPrefix: Boolean get() = textConfig.usePingAsPrefix

    /**
     * Returns the preferred prefix for triggering this bot
     *
     * @return The preferred prefix
     */
    val prefix: String
        get() = when {
            isPingAsPrefix -> jda.selfUser.asMention + " "
            else -> prefixes.first()
        }

    //TODO docs
    /**
     * Returns the [DefaultEmbedSupplier]
     *
     * @return The [DefaultEmbedSupplier]
     * @see DefaultEmbedSupplier
     */
    val defaultEmbedSupplier: DefaultEmbedSupplier

    //TODO docs
    /**
     * Returns the [DefaultEmbedFooterIconSupplier]
     *
     * @return The [DefaultEmbedFooterIconSupplier]
     * @see DefaultEmbedFooterIconSupplier
     */
    val defaultEmbedFooterIconSupplier: DefaultEmbedFooterIconSupplier

    /**
     * Returns the help builder consumer - changes the EmbedBuilder given to add more stuff in it
     *
     * @return The help builder consumer
     */
    val helpBuilderConsumer: HelpBuilderConsumer?
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
     * @param autocompleteHandlerName The name of the autocomplete handler, supplied at [AutocompleteHandler.name]
     */
    fun invalidateAutocompleteCache(autocompleteHandlerName: String)
    //endregion
}
