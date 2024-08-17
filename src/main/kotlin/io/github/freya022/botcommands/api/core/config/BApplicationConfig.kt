package io.github.freya022.botcommands.api.core.config

import io.github.freya022.botcommands.api.commands.application.annotations.Test
import io.github.freya022.botcommands.api.commands.application.diff.DiffEngine
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.CacheAutocomplete
import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.api.core.utils.loggerOf
import io.github.freya022.botcommands.api.core.utils.toImmutableList
import io.github.freya022.botcommands.api.core.utils.unmodifiableView
import io.github.freya022.botcommands.api.localization.providers.DefaultLocalizationMapProvider
import io.github.freya022.botcommands.api.localization.readers.DefaultJsonLocalizationMapReader
import io.github.freya022.botcommands.api.localization.readers.LocalizationMapReader
import io.github.freya022.botcommands.internal.core.config.ConfigDSL
import io.github.freya022.botcommands.internal.core.config.ConfigurationValue
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction

@InjectedService
interface BApplicationConfig {
    /**
     * Whether application commands should be listened for.
     *
     * Default: `true`
     *
     * Spring property: `botcommands.application.enable`
     */
    @ConfigurationValue(path = "botcommands.application.enable", defaultValue = "true")
    val enable: Boolean

    /**
     * If not empty, only these guilds will have their application commands updated.
     *
     * Existing commands won't be removed in other guilds, global commands will still be updated.
     *
     * Spring property: `botcommands.application.slashGuildIds`
     */
    @ConfigurationValue(path = "botcommands.application.slashGuildIds")
    val slashGuildIds: List<Long>

    /**
     * Test guilds IDs for all commands annotated with [Test]
     *
     * Spring property: `botcommands.application.testGuildIds`
     *
     * @see Test @Test
     */
    @ConfigurationValue(path = "botcommands.application.testGuildIds")
    val testGuildIds: List<Long>

    /**
     * Disables autocomplete caching, unless [CacheAutocomplete.forceCache] is set to `true`.
     *
     * This could be useful when testing methods that use autocomplete caching while using hotswap.
     *
     * Default: `false`
     *
     * Spring property: `botcommands.application.disableAutocompleteCache`
     */
    @ConfigurationValue(path = "botcommands.application.disableAutocompleteCache", defaultValue = "false")
    val disableAutocompleteCache: Boolean

    /**
     * Enables the library to compare local commands against Discord's command,
     * to check if application commands need to be updated.
     *
     * The default behavior is to compare the command data to what has been locally saved,
     * as it does not require any request, and is therefore way faster.
     *
     * The issue with local checks is that you could update commands on another machine,
     * while the other machine is not aware of it.
     *
     * Which is why you should use online checks during development,
     * but local checks in production, as they avoid requests and aren't run on multiple machines.
     *
     * **Note**: This does not enable you to run two instances simultaneously.
     *
     * Default: `false`
     *
     * Spring property: `botcommands.application.onlineAppCommandCheckEnabled`
     */
    @ConfigurationValue(path = "botcommands.application.onlineAppCommandCheckEnabled", defaultValue = "false")
    val onlineAppCommandCheckEnabled: Boolean

    /**
     * The diff engine to use when comparing old and new application commands,
     * to determine if commands needs to be updated.
     *
     * Only change this if necessary.
     *
     * Default: [DiffEngine.NEW]
     *
     * Spring property: `botcommands.application.diffEngine`
     */
    @ConfigurationValue(path = "botcommands.application.diffEngine", defaultValue = "new")
    val diffEngine: DiffEngine

    /**
     * Whether the raw JSON of the application commands should be logged when an update is required.
     *
     * Default: `false`
     *
     * Spring property: `botcommands.application.logApplicationCommandData`
     */
    @ConfigurationValue(path = "botcommands.application.logApplicationCommandData", defaultValue = "false")
    val logApplicationCommandData: Boolean

    /**
     * Sets whether all application commands should be guild-only, regardless of the command scope on the annotation.
     *
     * **Beware**: This also means that your global application commands will **not** be registered.
     *
     * **Note:** This only works on **annotated** commands,
     * as you can return when manually declaring with the DSL
     *
     * Default: `false`
     *
     * Spring property: `botcommands.application.forceGuildCommands`
     */
    @ConfigurationValue(path = "botcommands.application.forceGuildCommands", defaultValue = "false")
    val forceGuildCommands: Boolean

    /**
     * Mappings between the base bundle name and the locales it supports.
     *
     * The file can be anywhere and of any extension,
     * as long as it can be read by a [LocalizationMapReader],
     * see the [default implementation][DefaultJsonLocalizationMapReader].
     *
     * To know the final name of your file,
     * which in most cases is `<name>_<language>_<country>.<extension>`,
     * you can take a look at the [language tag][DiscordLocale.getLocale] of your [DiscordLocale],
     * replacing the `-` (hyphen) by a `_` (underscore).
     *
     * For example, `MyCommands` -> `[DiscordLocale.GERMAN, DiscordLocale.FRENCH, DiscordLocale.SPANISH]`
     * will, by default, read in the [`/bc_localization`][DefaultJsonLocalizationMapReader] folder:
     * - `DiscordLocale.GERMAN` -> `de` -> `MyCommands_de.json`
     * - `DiscordLocale.FRENCH` -> `fr` -> `MyCommands_fr.json`
     * - `DiscordLocale.SPANISH` -> `es-ES` -> `es_ES` -> `MyCommand_es_ES.json`
     *
     * Spring property: `botcommands.application.localizations` ;
     * Append the bundle name to the key, and have the values be the locales,
     * for example, `botcommands.application.localizations.MyBundle=english_us,german,french`.
     *
     * @see DefaultLocalizationMapProvider
     * @see DefaultJsonLocalizationMapReader
     */
    @ConfigurationValue(path = "botcommands.application.localizations")
    val baseNameToLocalesMap: Map<String, List<DiscordLocale>>

    /**
     * Whether to log a `WARN` if a localization key isn't found when registering the commands.
     *
     * They will occur when a bundle from [baseNameToLocalesMap] doesn't contain
     * a value for a key requested by [LocalizationFunction],
     * such as command name/description, option name/description, choice name...
     *
     * Default: `false`
     *
     * Spring property: `botcommands.application.logMissingLocalizationKeys`
     */
    @ConfigurationValue(path = "botcommands.application.logMissingLocalizationKeys", defaultValue = "false")
    val logMissingLocalizationKeys: Boolean
}

@ConfigDSL
class BApplicationConfigBuilder internal constructor() : BApplicationConfig {
    @set:JvmName("enable")
    override var enable: Boolean = true
    override val slashGuildIds: MutableList<Long> = mutableListOf()
    override val testGuildIds: MutableList<Long> = mutableListOf()
    @set:DevConfig
    @set:JvmName("disableAutocompleteCache")
    override var disableAutocompleteCache = false
    @set:DevConfig
    @set:JvmName("enableOnlineAppCommandChecks")
    override var onlineAppCommandCheckEnabled: Boolean = false
    @set:DevConfig
    override var diffEngine: DiffEngine = DiffEngine.NEW
    override var logApplicationCommandData: Boolean = false
    @set:DevConfig
    @set:JvmName("forceGuildCommands")
    override var forceGuildCommands: Boolean = false

    private val _baseNameToLocalesMap: MutableMap<String, MutableList<DiscordLocale>> = hashMapOf()
    override val baseNameToLocalesMap: Map<String, List<DiscordLocale>> = _baseNameToLocalesMap.unmodifiableView()

    override var logMissingLocalizationKeys: Boolean = false

    /**
     * Adds the specified bundle names with its locales;
     * those bundles will be used for command localization (name, description, options, choices...).
     *
     * All the locales will be considered as pointing to a valid localization bundle,
     * logging a warning if it can't be found.
     *
     * ### Localization keys
     *
     * The keys are the same as those generated by [LocalizationFunction].
     *
     * ### Localization file
     *
     * The file can be anywhere and of any extension,
     * as long as it can be read by a [LocalizationMapReader],
     * see the [default implementation][DefaultJsonLocalizationMapReader].
     *
     * To know the final name of your file,
     * which in most cases is `<name>_<language>_<country>.<extension>`,
     * you can take a look at the [language tag][DiscordLocale.getLocale] of your [DiscordLocale],
     * replacing the `-` (hyphen) by a `_` (underscore).
     *
     * For example, `MyCommands` -> `[DiscordLocale.GERMAN, DiscordLocale.FRENCH, DiscordLocale.SPANISH]`
     * will, by default, read in the `/bc_localization` ([configurable][DefaultJsonLocalizationMapReader]) folder:
     * - `DiscordLocale.GERMAN` -> `de` -> `MyCommands_de.json`
     * - `DiscordLocale.FRENCH` -> `fr` -> `MyCommands_fr.json`
     * - `DiscordLocale.SPANISH` -> `es-ES` -> `es_ES` -> `MyCommand_es_ES.json`
     *
     * See [DefaultLocalizationMapProvider] and [DefaultJsonLocalizationMapReader] for default implementation details.
     *
     * ### Spring property
     * The property `botcommands.application.localizations` is suffixed with the bundle name to the key,
     * and the value is an array of [DiscordLocale], for example,
     * `botcommands.application.localizations.MyBundle=english_us,german,french`.
     *
     * @param bundleName The name of the localization bundle
     * @param locales    The locales the localization bundle supports
     *
     * @see DefaultLocalizationMapProvider
     * @see DefaultJsonLocalizationMapReader
     * @see LocalizationFunction
     */
    fun addLocalizations(bundleName: String, locales: List<DiscordLocale>) {
        _baseNameToLocalesMap.computeIfAbsent(bundleName) { ArrayList() } += locales
    }

    /**
     * Adds the specified bundle names with its locales;
     * those bundles will be used for command localization (name, description, options, choices...).
     *
     * All the locales will be considered as pointing to a valid localization bundle,
     * logging a warning if it can't be found.
     *
     * ### Localization keys
     *
     * The keys are the same as those generated by [LocalizationFunction].
     *
     * ### Localization file
     *
     * The file can be anywhere and of any extension,
     * as long as it can be read by a [LocalizationMapReader],
     * see the [default implementation][DefaultJsonLocalizationMapReader].
     *
     * To know the final name of your file,
     * which in most cases is `<name>_<language>_<country>.<extension>`,
     * you can take a look at the [language tag][DiscordLocale.getLocale] of your [DiscordLocale],
     * replacing the `-` (hyphen) by a `_` (underscore).
     *
     * For example, `MyCommands` -> `[DiscordLocale.GERMAN, DiscordLocale.FRENCH, DiscordLocale.SPANISH]`
     * will, by default, read in the `/bc_localization` ([configurable][DefaultJsonLocalizationMapReader]) folder:
     * - `DiscordLocale.GERMAN` -> `de` -> `MyCommands_de.json`
     * - `DiscordLocale.FRENCH` -> `fr` -> `MyCommands_fr.json`
     * - `DiscordLocale.SPANISH` -> `es-ES` -> `es_ES` -> `MyCommand_es_ES.json`
     *
     * See [DefaultLocalizationMapProvider] and [DefaultJsonLocalizationMapReader] for default implementation details.
     *
     * ### Spring property
     * The property `botcommands.application.localizations` is suffixed with the bundle name to the key,
     * and the value is an array of [DiscordLocale], for example,
     * `botcommands.application.localizations.MyBundle=english_us,german,french`.
     *
     * @param bundleName The name of the localization bundle
     * @param locales    The locales the localization bundle supports
     *
     * @see DefaultLocalizationMapProvider
     * @see DefaultJsonLocalizationMapReader
     * @see LocalizationFunction
     */
    fun addLocalizations(bundleName: String, vararg locales: DiscordLocale) {
        addLocalizations(bundleName, locales.asList())
    }

    @JvmSynthetic
    internal fun build(): BApplicationConfig {
        val logger = KotlinLogging.loggerOf<BApplicationConfig>()
        if (disableAutocompleteCache)
            logger.info { "Disabled autocomplete cache, except forced caches" }

        return object : BApplicationConfig {
            override val enable = this@BApplicationConfigBuilder.enable
            override val slashGuildIds = this@BApplicationConfigBuilder.slashGuildIds.toImmutableList()
            override val testGuildIds = this@BApplicationConfigBuilder.testGuildIds.toImmutableList()
            override val disableAutocompleteCache = this@BApplicationConfigBuilder.disableAutocompleteCache
            override val onlineAppCommandCheckEnabled = this@BApplicationConfigBuilder.onlineAppCommandCheckEnabled
            override val diffEngine = this@BApplicationConfigBuilder.diffEngine
            override val logApplicationCommandData = this@BApplicationConfigBuilder.logApplicationCommandData
            override val forceGuildCommands = this@BApplicationConfigBuilder.forceGuildCommands
            override val baseNameToLocalesMap =
                this@BApplicationConfigBuilder.baseNameToLocalesMap.mapValues { (_, v) -> v.toImmutableList() }
                    .unmodifiableView()
            override val logMissingLocalizationKeys = this@BApplicationConfigBuilder.logMissingLocalizationKeys
        }
    }
}