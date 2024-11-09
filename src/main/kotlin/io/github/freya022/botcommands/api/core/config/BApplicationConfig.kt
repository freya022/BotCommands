package io.github.freya022.botcommands.api.core.config

import io.github.freya022.botcommands.api.ReceiverConsumer
import io.github.freya022.botcommands.api.commands.application.annotations.RequiresApplicationCommands
import io.github.freya022.botcommands.api.commands.application.annotations.Test
import io.github.freya022.botcommands.api.commands.application.diff.DiffEngine
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.CacheAutocomplete
import io.github.freya022.botcommands.api.core.Logging
import io.github.freya022.botcommands.api.core.config.application.cache.ApplicationCommandsCacheConfig
import io.github.freya022.botcommands.api.core.config.application.cache.ApplicationCommandsCacheConfig.LogDataIf
import io.github.freya022.botcommands.api.core.config.application.cache.ApplicationCommandsCacheConfigBuilder
import io.github.freya022.botcommands.api.core.config.application.cache.DatabaseApplicationCommandsCacheConfigBuilder
import io.github.freya022.botcommands.api.core.config.application.cache.FileApplicationCommandsCacheConfigBuilder
import io.github.freya022.botcommands.api.core.db.ConnectionSupplier
import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.api.core.utils.loggerOf
import io.github.freya022.botcommands.api.core.utils.toImmutableList
import io.github.freya022.botcommands.api.core.utils.unmodifiableView
import io.github.freya022.botcommands.api.localization.providers.DefaultLocalizationMapProvider
import io.github.freya022.botcommands.api.localization.readers.DefaultJsonLocalizationMapReader
import io.github.freya022.botcommands.api.localization.readers.LocalizationMapReader
import io.github.freya022.botcommands.internal.core.config.ConfigDSL
import io.github.freya022.botcommands.internal.core.config.ConfigurationValue
import io.github.freya022.botcommands.internal.core.config.DeprecatedValue
import io.github.freya022.botcommands.internal.core.exceptions.internalErrorMessage
import io.github.freya022.botcommands.internal.utils.lazyWritable
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction
import java.nio.file.Path
import kotlin.io.path.Path

@InjectedService
interface BApplicationConfig {
    /**
     * Whether application commands should be listened for.
     *
     * You can use [@RequiresApplicationCommands][RequiresApplicationCommands]
     * to disable services when this is set to `false`.
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
     * Configuration of the application commands cache.
     *
     * Helps avoid request to Discord as commands do not need to be updated most of the time.
     *
     * Default: [`fileCache()`][BApplicationConfigBuilder.fileCache]
     *
     * @see BApplicationConfigBuilder.fileCache
     * @see BApplicationConfigBuilder.databaseCache
     * @see BApplicationConfigBuilder.disableCache
     */
    val cache: ApplicationCommandsCacheConfig?

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
    @Deprecated(
        message = "Moved to 'checkOnline' of the 'cache' property",
        replaceWith = ReplaceWith("cache?.checkOnline")
    )
    @ConfigurationValue(path = "botcommands.application.onlineAppCommandCheckEnabled", defaultValue = "false")
    @DeprecatedValue("Moved to the 'cache' prefix", replacement = "botcommands.application.cache.checkOnline")
    val onlineAppCommandCheckEnabled: Boolean
        get() = cache?.checkOnline ?: false

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
    @Deprecated(
        message = "Moved to 'diffEngine' of the 'cache' property",
        replaceWith = ReplaceWith("cache?.diffEngine")
    )
    @ConfigurationValue(path = "botcommands.application.diffEngine", defaultValue = "new")
    @DeprecatedValue("Moved to the 'cache' prefix", replacement = "botcommands.application.cache.diffEngine")
    val diffEngine: DiffEngine
        get() = cache?.diffEngine ?: DiffEngine.NEW

    /**
     * Whether the raw JSON of the application commands should be logged when an update is required.
     *
     * Default: `false`
     *
     * Spring property: `botcommands.application.logApplicationCommandData`
     */
    @Deprecated(message = "Moved to 'logDataIf' of the 'cache' property", replaceWith = ReplaceWith("cache?.logDataIf"))
    @ConfigurationValue(path = "botcommands.application.logApplicationCommandData", defaultValue = "false")
    @DeprecatedValue("Moved to the 'cache' prefix", replacement = "botcommands.application.cache.logDataIf")
    val logApplicationCommandData: Boolean
        get() = when (cache?.logDataIf) {
            LogDataIf.CHANGED, LogDataIf.ALWAYS -> true
            else -> false
        }

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

    // Prevent possible warnings if the default cache isn't used
    override var cache: ApplicationCommandsCacheConfigBuilder? by lazyWritable { FileApplicationCommandsCacheConfigBuilder(getDefaultCachePath()) }
        private set

    @Deprecated("Moved to 'checkOnline' of fileCache(...)/databaseCache(...)")
    @set:DevConfig
    @set:JvmName("enableOnlineAppCommandChecks")
    override var onlineAppCommandCheckEnabled: Boolean
        @Suppress("DEPRECATION")
        get() = super.onlineAppCommandCheckEnabled
        set(value) {
            checkNotNull(cache) { "The cache is disabled" }
            cache!!.checkOnline = value
        }
    @Deprecated("Moved to 'diffEngine' of fileCache(...)/databaseCache(...)")
    @set:DevConfig
    override var diffEngine: DiffEngine
        @Suppress("DEPRECATION")
        get() = super.diffEngine
        set(value) {
            checkNotNull(cache) { "The cache is disabled" }
            cache!!.diffEngine = value
        }
    @Deprecated("Moved to 'logDataIf' of fileCache(...)/databaseCache(...)")
    override var logApplicationCommandData: Boolean
        @Suppress("DEPRECATION")
        get() = super.logApplicationCommandData
        set(value) {
            checkNotNull(cache) { "The cache is disabled" }
            cache!!.logDataIf = if (value) LogDataIf.CHANGED else LogDataIf.NEVER
        }
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

    /**
     * Configures a file-based cache for application commands,
     * which helps avoid request to Discord as commands do not need to be updated most of the time.
     *
     * This is the default cache strategy, however,
     * if you use a database I'd recommend using [databaseCache],
     * as it would be more resilient to write issues.
     *
     * ### Cache path
     *
     * The default cache folder is at:
     * - Windows: `%AppData%/BotCommands`
     * - Unix: `/var/tmp/BotCommands`
     *
     * Each application has a folder inside it, meaning you can safely share this folder with other applications.
     *
     * ### Docker
     *
     * If your app runs in a container, you will need to change the [path]
     * to a volume (recommended) or a bind-mount,
     * you can alternatively use [databaseCache] with a separate PostgreSQL service.
     *
     * @param path The folder in which to save application commands
     *
     * @see databaseCache
     */
    @JvmOverloads
    fun fileCache(path: Path = getDefaultCachePath(), block: ReceiverConsumer<FileApplicationCommandsCacheConfigBuilder> = ReceiverConsumer.noop()) {
        cache = FileApplicationCommandsCacheConfigBuilder(path).apply(block)
    }

    /**
     * Configures a cache for application commands, stored in the database supplied by [ConnectionSupplier].
     *
     * This is recommended if you use a container (to avoid having to manage more files),
     * or to avoid write issues.
     */
    fun databaseCache(block: ReceiverConsumer<DatabaseApplicationCommandsCacheConfigBuilder> = ReceiverConsumer.noop()) {
        cache = DatabaseApplicationCommandsCacheConfigBuilder().apply(block)
    }

    /**
     * Entirely disables the application commands cache,
     * meaning the application commands will always be updated on startup.
     *
     * Do not use on your production bot unless **absolutely** necessary.
     */
    @DevConfig
    fun disableCache() {
        cache = null
    }

    private fun getDefaultCachePath(): Path {
        val osName = System.getProperty("os.name")
        fun envPath(name: String, fallbackName: String? = null): Path {
            val envValue = System.getenv(name)
                ?: fallbackName?.let(System::getenv)
                ?: throwInternal("Absent environment variable '$name' (fallback '$fallbackName') in OS '$osName'")
            return Path(envValue)
        }

        return when {
            osName.startsWith("Windows") -> envPath("appdata").resolve("BotCommands")
            // https://specifications.freedesktop.org/basedir-spec/latest/
            osName.startsWith("Linux") -> envPath("XDG_DATA_HOME", "HOME").resolve(".local/share/BotCommands")
            // https://developer.apple.com/library/archive/documentation/FileManagement/Conceptual/FileSystemProgrammingGuide/MacOSXDirectories/MacOSXDirectories.html
            // https://developer.apple.com/library/archive/documentation/FileManagement/Conceptual/FileSystemProgrammingGuide/FileSystemOverview/FileSystemOverview.html#//apple_ref/doc/uid/TP40010672-CH2-SW1
            osName.startsWith("Mac") || osName.startsWith("Darwin") -> Path("~/Library/Application Support/io.github.freya022.BotCommands")
            else -> {
                Logging.currentLogger().warn { internalErrorMessage("Unsupported OS '$osName' for file-based application commands cache, using fallback") }

                Path("/var/tmp/BotCommands")
            }
        }
    }

    @JvmSynthetic
    internal fun build(): BApplicationConfig {
        val logger = KotlinLogging.loggerOf<BApplicationConfig>()
        if (disableAutocompleteCache)
            logger.info { "Disabled autocomplete cache, except forced caches" }
        if (cache == null)
            logger.info { "Disabled application commands caching, this could be expensive if you have a lot of guilds!" }

        return object : BApplicationConfig {
            override val enable = this@BApplicationConfigBuilder.enable
            override val slashGuildIds = this@BApplicationConfigBuilder.slashGuildIds.toImmutableList()
            override val testGuildIds = this@BApplicationConfigBuilder.testGuildIds.toImmutableList()
            override val disableAutocompleteCache = this@BApplicationConfigBuilder.disableAutocompleteCache
            override val cache = this@BApplicationConfigBuilder.cache?.build()
            override val forceGuildCommands = this@BApplicationConfigBuilder.forceGuildCommands
            override val baseNameToLocalesMap =
                this@BApplicationConfigBuilder.baseNameToLocalesMap.mapValues { (_, v) -> v.toImmutableList() }
                    .unmodifiableView()
            override val logMissingLocalizationKeys = this@BApplicationConfigBuilder.logMissingLocalizationKeys
        }
    }
}