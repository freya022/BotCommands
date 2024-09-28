package io.github.freya022.botcommands.internal.core.config

import io.github.freya022.botcommands.api.commands.application.diff.DiffEngine
import io.github.freya022.botcommands.api.core.config.*
import io.github.freya022.botcommands.api.core.config.application.cache.ApplicationCommandsCacheConfig
import io.github.freya022.botcommands.api.core.config.application.cache.ApplicationCommandsCacheConfigBuilder
import io.github.freya022.botcommands.api.utils.EmojiUtils
import io.github.freya022.botcommands.internal.utils.throwArgument
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.requests.GatewayIntent
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.Name
import kotlin.io.path.Path
import kotlin.time.Duration
import kotlin.time.toKotlinDuration
import java.time.Duration as JavaDuration

@ConfigurationProperties(prefix = "botcommands.core", ignoreUnknownFields = false)
internal class BotCommandsCoreConfiguration(
    override val predefinedOwnerIds: Set<Long> = emptySet(),
    override val packages: Set<String> = emptySet(),
    override val classes: Set<Class<*>> = emptySet(),
    override val disableExceptionsInDMs: Boolean = false,
    override val enableOwnerBypass: Boolean = false,
    @Name("disableAutocompleteCache")
    @Suppress("PropertyName")
    internal val _disableAutocompleteCache: Boolean? = null,
    override val ignoredIntents: Set<GatewayIntent> = emptySet(),
    override val ignoredEventIntents: Set<Class<out Event>> = emptySet(),
    override val ignoreRestRateLimiter: Boolean = false,
) : BConfig {
    override val classGraphProcessors: Nothing get() = unusable()
    override val debugConfig: Nothing get() = unusable()
    override val serviceConfig: Nothing get() = unusable()
    override val databaseConfig: Nothing get() = unusable()
    override val localizationConfig: Nothing get() = unusable()
    override val textConfig: Nothing get() = unusable()
    override val applicationConfig: Nothing get() = unusable()
    override val modalsConfig: Nothing get() = unusable()
    override val componentsConfig: Nothing get() = unusable()
    override val coroutineScopesConfig: Nothing get() = unusable()
}

@OptIn(DevConfig::class)
internal fun BConfigBuilder.applyConfig(configuration: BotCommandsCoreConfiguration) = apply {
    predefinedOwnerIds += configuration.predefinedOwnerIds
    packages += configuration.packages
    classes += configuration.classes
    disableExceptionsInDMs = configuration.disableExceptionsInDMs
    enableOwnerBypass = configuration.enableOwnerBypass
    @Suppress("DEPRECATION")
    configuration._disableAutocompleteCache?.let { disableAutocompleteCache = it }
    ignoredIntents += configuration.ignoredIntents
    ignoredEventIntents += configuration.ignoredEventIntents
    ignoreRestRateLimiter = configuration.ignoreRestRateLimiter
}

@ConfigurationProperties(prefix = "botcommands.database", ignoreUnknownFields = false)
internal class BotCommandsDatabaseConfiguration(
    override val dumpLongTransactions: Boolean = false,
    override val logQueries: Boolean = false,
    override val logQueryParameters: Boolean = true,
    queryLogThreshold: JavaDuration? = null
) : BDatabaseConfig {
    override val queryLogThreshold: Duration = queryLogThreshold?.toKotlinDuration() ?: Duration.INFINITE
}

@OptIn(DevConfig::class)
internal fun BDatabaseConfigBuilder.applyConfig(configuration: BotCommandsDatabaseConfiguration) = apply {
    dumpLongTransactions = configuration.dumpLongTransactions
    logQueries = configuration.logQueries
    logQueryParameters = configuration.logQueryParameters
    queryLogThreshold = configuration.queryLogThreshold
}

@ConfigurationProperties(prefix = "botcommands.text", ignoreUnknownFields = false)
internal class BotCommandsTextConfiguration(
    override val enable: Boolean = true,
    override val usePingAsPrefix: Boolean = false,
    override val prefixes: List<String> = emptyList(),
    override val isHelpDisabled: Boolean = false,
    override val showSuggestions: Boolean = true,
    @Name("dmClosedEmoji")
    internal val dmClosedEmojiString: String? = null
) : BTextConfig {
    override val dmClosedEmoji: Nothing get() = unusable()
}

internal fun BTextConfigBuilder.applyConfig(configuration: BotCommandsTextConfiguration) = apply {
    enable = configuration.enable
    usePingAsPrefix = configuration.usePingAsPrefix
    prefixes += configuration.prefixes
    isHelpDisabled = configuration.isHelpDisabled
    showSuggestions = configuration.showSuggestions
    configuration.dmClosedEmojiString?.let { dmClosedEmojiSupplier = { EmojiUtils.resolveJDAEmoji(it) } }
}

@ConfigurationProperties(prefix = "botcommands.localization", ignoreUnknownFields = false)
internal class BotCommandsLocalizationConfiguration(
    override val responseBundles: Set<String> = emptySet(),
) : BLocalizationConfig

internal fun BLocalizationConfigBuilder.applyConfig(configuration: BotCommandsLocalizationConfiguration) = apply {
    responseBundles += configuration.responseBundles
}

@ConfigurationProperties(prefix = "botcommands.application", ignoreUnknownFields = true)
internal class BotCommandsApplicationConfiguration(
    override val enable: Boolean = true,
    override val slashGuildIds: List<Long> = emptyList(),
    override val testGuildIds: List<Long> = emptyList(),
    override val disableAutocompleteCache: Boolean = false,
    @Name("onlineAppCommandCheckEnabled")
    @Suppress("PropertyName")
    internal val _onlineAppCommandCheckEnabled: Boolean? = null,
    @Name("diffEngine")
    @Suppress("PropertyName")
    internal val _diffEngine: DiffEngine? = null,
    @Name("logApplicationCommandData")
    @Suppress("PropertyName")
    internal val _logApplicationCommandData: Boolean? = null,
    override val forceGuildCommands: Boolean = false,
    localizations: Map<String, List<DiscordLocale>> = emptyMap(),
    override val logMissingLocalizationKeys: Boolean = false,
    @Name("cache")
    internal val springCache: Cache = Cache(),
) : BApplicationConfig {
    override val cache: Nothing get() = unusable()

    class Cache(
        /**
         * Type of application commands cache.
         *
         * Please see the different types of cache in [BApplicationConfigBuilder].
         *
         * @see BApplicationConfigBuilder.fileCache
         * @see BApplicationConfigBuilder.databaseCache
         * @see BApplicationConfigBuilder.disableCache
         */
        @ConfigurationValue("botcommands.application.cache.type", defaultValue = "file")
        val type: Type = Type.FILE,
        val file: File = File(),
        val database: Database = Database(),
        val checkOnline: Boolean = false,
        val diffEngine: DiffEngine = DiffEngine.NEW,
        val logDataIf: ApplicationCommandsCacheConfig.LogDataIf = ApplicationCommandsCacheConfig.LogDataIf.CHANGED
    ) {
        enum class Type {
            FILE,
            DATABASE,
            NULL
        }

        // Check those properties are checked for unknown fields
        class File(
            val path: String? = null
        )

        class Database(
            // properties
        )
    }

    override val baseNameToLocalesMap = localizations
}

@Suppress("DEPRECATION")
@OptIn(DevConfig::class)
internal fun BApplicationConfigBuilder.applyConfig(configuration: BotCommandsApplicationConfiguration) = apply {
    enable = configuration.enable
    slashGuildIds += configuration.slashGuildIds
    testGuildIds += configuration.testGuildIds
    disableAutocompleteCache = configuration.disableAutocompleteCache
    configureCache(configuration)
    configuration._onlineAppCommandCheckEnabled?.let { onlineAppCommandCheckEnabled = it }
    configuration._diffEngine?.let { diffEngine = it }
    configuration._logApplicationCommandData?.let { logApplicationCommandData = it }
    forceGuildCommands = configuration.forceGuildCommands
    configuration.baseNameToLocalesMap.forEach(::addLocalizations)
    logMissingLocalizationKeys = configuration.logMissingLocalizationKeys
}

@OptIn(DevConfig::class)
private fun BApplicationConfigBuilder.configureCache(configuration: BotCommandsApplicationConfiguration) {
    fun ApplicationCommandsCacheConfigBuilder.applyConfig(cache: BotCommandsApplicationConfiguration.Cache) {
        checkOnline = cache.checkOnline
        diffEngine = cache.diffEngine
        logDataIf = cache.logDataIf
    }
    when (configuration.springCache.type) {
        BotCommandsApplicationConfiguration.Cache.Type.FILE -> {
            val file = configuration.springCache.file
            when {
                file.path == null -> fileCache {
                    applyConfig(configuration.springCache)
                }
                else -> fileCache(Path(file.path)) {
                    applyConfig(configuration.springCache)
                }
            }
        }
        BotCommandsApplicationConfiguration.Cache.Type.DATABASE -> databaseCache {
            applyConfig(configuration.springCache)
        }
        BotCommandsApplicationConfiguration.Cache.Type.NULL -> disableCache()
    }
}

@ConfigurationProperties(prefix = "botcommands.modals", ignoreUnknownFields = true)
internal class BotCommandsModalsConfiguration(
    override val enable: Boolean = true,
) : BModalsConfig {

}

internal fun BModalsConfigBuilder.applyConfig(configuration: BotCommandsModalsConfiguration) = apply {
    enable = configuration.enable
}

@ConfigurationProperties(prefix = "botcommands.components", ignoreUnknownFields = false)
internal class BotCommandsComponentsConfiguration(
    override val enable: Boolean = false
) : BComponentsConfig

internal fun BComponentsConfigBuilder.applyConfig(configuration: BotCommandsComponentsConfiguration) = apply {
    enable = configuration.enable
}

private fun unusable(): Nothing = throwArgument("Cannot be used")
