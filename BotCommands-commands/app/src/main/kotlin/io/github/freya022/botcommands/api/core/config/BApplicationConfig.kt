package io.github.freya022.botcommands.api.core.config

import io.github.freya022.botcommands.api.ReceiverConsumer
import io.github.freya022.botcommands.api.commands.application.annotations.RequiresApplicationCommands
import io.github.freya022.botcommands.api.commands.application.annotations.Test
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.ForceAutocompleteCache
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.cache.factory.builder.AutocompleteCacheFactoryBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.Logging
import io.github.freya022.botcommands.api.core.config.application.cache.ApplicationCommandsCacheConfig
import io.github.freya022.botcommands.api.core.config.application.cache.ApplicationCommandsCacheConfigBuilder
import io.github.freya022.botcommands.api.core.config.application.cache.DatabaseApplicationCommandsCacheConfigBuilder
import io.github.freya022.botcommands.api.core.config.application.cache.FileApplicationCommandsCacheConfigBuilder
import io.github.freya022.botcommands.api.core.db.ConnectionSupplier
import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.api.core.utils.loggerOf
import io.github.freya022.botcommands.api.core.utils.toImmutableList
import io.github.freya022.botcommands.api.core.utils.unmodifiableView
import io.github.freya022.botcommands.api.localization.providers.DefaultLocalizationMapProvider
import io.github.freya022.botcommands.api.localization.readers.JacksonLocalizationMapReader
import io.github.freya022.botcommands.api.localization.readers.LocalizationMapReader
import io.github.freya022.botcommands.internal.core.config.ConfigDSL
import io.github.freya022.botcommands.internal.core.config.ConfigurationValue
import io.github.freya022.botcommands.internal.core.exceptions.internalErrorMessage
import io.github.freya022.botcommands.internal.utils.lazyWritable
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction
import java.nio.file.Path
import kotlin.io.path.Path

/**
 * Configuration for the application commands feature.
 *
 * A configuration of this feature must be registered for it to be active.
 *
 * Spring users can set the `botcommands.application.enable` property to `false` to disable this feature,
 * as adding the dependency will enable it by default.
 *
 * [@RequiresApplicationCommands][RequiresApplicationCommands] can be used to disable services when this feature isn't registered.
 *
 * @see [BApplicationConfig.builder]
 * @see [registerApplicationCommands]
 */
@InjectedService
interface BApplicationConfig : IConfig, BApplicationConfigProps {

    override val configType get() = BApplicationConfig::class.java

    companion object {
        /**
         * Creates a new [BApplicationConfigBuilder], you must [build][BApplicationConfigBuilder.build] it and [register][BConfigBuilder.registerModule] it.
         */
        @JvmStatic
        fun builder(): BApplicationConfigBuilder {
            return BApplicationConfigBuilder.create()
        }
    }
}

internal val BContext.applicationConfig: BApplicationConfig
    get() = config.getConfigOrNull<BApplicationConfig>()
        ?: throwInternal("")

interface BApplicationConfigProps {

    /**
     * If not empty, application commands will only be updated in these guilds.
     *
     * Existing commands won't be removed in other guilds, global commands will still be updated.
     *
     * Spring property: `botcommands.application.guildsToUpdate`
     */
    @get:ConfigurationValue(
        path = "botcommands.application.guildsToUpdate",
        description = "If not empty, application commands will only be updated in these guilds. Existing commands won't be removed in other guilds, global commands will still be updated.",
    )
    val guildsToUpdate: List<Long>

    /**
     * Test guilds IDs for all commands annotated with [Test].
     *
     * Spring property: `botcommands.application.testGuildIds`
     *
     * @see Test @Test
     */
    @get:ConfigurationValue(
        path = "botcommands.application.testGuildIds",
        description = "Test guilds IDs for all commands annotated with [Test].",
    )
    val testGuildIds: List<Long>

    /**
     * Disables autocomplete caching, unless [@ForceAutocompleteCache][ForceAutocompleteCache]/[AutocompleteCacheFactoryBuilder.forceCache][AutocompleteCacheFactoryBuilder.forceCache] is used.
     *
     * This could be useful when testing methods that use autocomplete caching while using hotswap.
     *
     * Default: `false`
     *
     * Spring property: `botcommands.application.disableAutocompleteCache`
     */
    @get:ConfigurationValue(
        path = "botcommands.application.disableAutocompleteCache",
        description = "Disables autocomplete caching, unless [CacheAutocomplete.forceCache] is set to `true`.",
        defaultValue = "false",
    )
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
     * Whether all application commands should be registered on each guild,
     * regardless of the command scope on the annotation.
     *
     * **Note:** This only works on **annotated** commands, you will need to handle code-declared commands.
     *
     * Default: `false`
     *
     * Spring property: `botcommands.application.forceGuildCommands`
     */
    @get:ConfigurationValue(
        path = "botcommands.application.forceGuildCommands",
        description = "Whether all application commands should be registered on each guild, regardless of the command scope on the annotation.",
        defaultValue = "false",
    )
    val forceGuildCommands: Boolean

    /**
     * Mappings between the base bundle name and the locales it supports.
     *
     * The file can be anywhere and of any extension,
     * as long as it can be read by a [LocalizationMapReader].
     *
     * To know the final name of your file,
     * which in most cases is `<name>_<language>_<country>.<extension>`,
     * you can take a look at the [language tag][DiscordLocale.getLocale] of your [DiscordLocale],
     * replacing the `-` (hyphen) by a `_` (underscore).
     *
     * For example, `MyCommands` -> `[DiscordLocale.GERMAN, DiscordLocale.FRENCH, DiscordLocale.SPANISH]`
     * will, by default, read in the [`/bc_localization`][JacksonLocalizationMapReader] folder:
     * - `DiscordLocale.GERMAN` -> `de` -> `MyCommands_de.json`
     * - `DiscordLocale.FRENCH` -> `fr` -> `MyCommands_fr.json`
     * - `DiscordLocale.SPANISH` -> `es-ES` -> `es_ES` -> `MyCommand_es_ES.json`
     *
     * Spring property: `botcommands.application.localizations` ;
     * Append the bundle name to the key, and have the values be the locales,
     * for example, `botcommands.application.localizations.MyBundle=english_us,german,french`.
     *
     * @see DefaultLocalizationMapProvider
     * @see JacksonLocalizationMapReader
     */
    @get:ConfigurationValue(
        path = "botcommands.application.localizations",
        description = "Map where the key is the base bundle name, and the values are the supported locales, see the docs of [BApplicationConfigBuilder#addLocalization].",
    )
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
    @get:ConfigurationValue(
        path = "botcommands.application.logMissingLocalizationKeys",
        description = "Whether to log a `WARN` if a localization key isn't found when registering the commands.",
        defaultValue = "false",
    )
    val logMissingLocalizationKeys: Boolean
}

/**
 * Builder of [BApplicationConfig].
 *
 * @see BApplicationConfig.builder
 */
@ConfigDSL
class BApplicationConfigBuilder private constructor() : BApplicationConfigProps {

    override val guildsToUpdate: MutableList<Long> = mutableListOf()
    override val testGuildIds: MutableList<Long> = mutableListOf()

    @set:DevConfig
    @set:JvmName("disableAutocompleteCache")
    override var disableAutocompleteCache = false

    // Prevent possible warnings if the default cache isn't used
    override var cache: ApplicationCommandsCacheConfigBuilder? by lazyWritable { FileApplicationCommandsCacheConfigBuilder(getDefaultCachePath()) }
        private set

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
     * see the [default implementation][JacksonLocalizationMapReader].
     *
     * To know the final name of your file,
     * which in most cases is `<name>_<language>_<country>.<extension>`,
     * you can take a look at the [language tag][DiscordLocale.getLocale] of your [DiscordLocale],
     * replacing the `-` (hyphen) by a `_` (underscore).
     *
     * For example, `MyCommands` -> `[DiscordLocale.GERMAN, DiscordLocale.FRENCH, DiscordLocale.SPANISH]`
     * will, by default, read in the `/bc_localization` folder:
     * - `DiscordLocale.GERMAN` -> `de` -> `MyCommands_de.json`
     * - `DiscordLocale.FRENCH` -> `fr` -> `MyCommands_fr.json`
     * - `DiscordLocale.SPANISH` -> `es-ES` -> `es_ES` -> `MyCommand_es_ES.json`
     *
     * See [DefaultLocalizationMapProvider] and [JacksonLocalizationMapReader] for default implementation details.
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
     * @see JacksonLocalizationMapReader
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
     * see the [default implementation][JacksonLocalizationMapReader].
     *
     * To know the final name of your file,
     * which in most cases is `<name>_<language>_<country>.<extension>`,
     * you can take a look at the [language tag][DiscordLocale.getLocale] of your [DiscordLocale],
     * replacing the `-` (hyphen) by a `_` (underscore).
     *
     * For example, `MyCommands` -> `[DiscordLocale.GERMAN, DiscordLocale.FRENCH, DiscordLocale.SPANISH]`
     * will, by default, read in the `/bc_localization` folder:
     * - `DiscordLocale.GERMAN` -> `de` -> `MyCommands_de.json`
     * - `DiscordLocale.FRENCH` -> `fr` -> `MyCommands_fr.json`
     * - `DiscordLocale.SPANISH` -> `es-ES` -> `es_ES` -> `MyCommand_es_ES.json`
     *
     * See [DefaultLocalizationMapProvider] and [JacksonLocalizationMapReader] for default implementation details.
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
     * @see JacksonLocalizationMapReader
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
     * - Windows: `%AppData%/BotCommands`,
     * - Linux: `$XDG_DATA_HOME/BotCommands` (fallbacks to `$HOME/.local/share/BotCommands`),
     * - macOS: `$HOME/Library/Application Support/io.github.freya022.BotCommands`
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
     *
     * ## Database requirements
     * The `BotCommands-database` module must be configured, see [BDatabaseConfig].
     *
     * The database must be a PostgreSQL instance, any recent version should do.
     *
     * ## Setting up the database schema
     * The tables required to store components are defined by the scripts in `db/bc-migration/app-commands`.
     *
     * It is recommended to use a migration tool to run these automatically, for example with Flyway:
     *
     * ```java
     * Flyway.configure(getClass().getClassLoader())
     *      .dataSource(source)
     *      .schemas("bc_commands_app")
     *      .locations("db/bc-migration/app-commands/postgresql")
     *      .load()
     *      .migrate();
     * ```
     * This will run all the migration scripts required to set up your database,
     * you can run this in the same class as your connection supplier.
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
            osName.startsWith("Mac") || osName.startsWith("Darwin") -> envPath("HOME").resolve("Library/Application Support/io.github.freya022.BotCommands")
            else -> {
                Logging.currentLogger().warn { internalErrorMessage("Unsupported OS '$osName' for file-based application commands cache, using fallback") }

                Path("/var/tmp/BotCommands")
            }
        }
    }

    fun build(): BApplicationConfig {
        val logger = KotlinLogging.loggerOf<BApplicationConfig>()
        if (disableAutocompleteCache)
            logger.info { "Disabled autocomplete cache, except forced caches" }
        if (cache == null)
            logger.info { "Disabled application commands caching, this could be expensive if you have a lot of guilds!" }

        return object : BApplicationConfig {
            override val guildsToUpdate = this@BApplicationConfigBuilder.guildsToUpdate.toImmutableList()
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

    internal companion object {
        @JvmSynthetic
        internal fun create(): BApplicationConfigBuilder = BApplicationConfigBuilder()
    }
}

/**
 * Registers the application commands feature.
 *
 * @param block A block for further configuration
 *
 * @see BApplicationConfig
 */
fun BConfigBuilder.registerApplicationCommands(block: BApplicationConfigBuilder.() -> Unit = { }) {
    val config = BApplicationConfigBuilder.create()
        .apply(block)
        .build()
    registerModule(config)
}
