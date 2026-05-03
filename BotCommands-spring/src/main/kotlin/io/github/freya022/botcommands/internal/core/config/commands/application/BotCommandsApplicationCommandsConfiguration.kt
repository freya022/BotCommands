package io.github.freya022.botcommands.internal.core.config.commands.application

import io.github.freya022.botcommands.api.commands.application.diff.DiffEngine
import io.github.freya022.botcommands.api.core.config.BApplicationConfig
import io.github.freya022.botcommands.api.core.config.BApplicationConfigBuilder
import io.github.freya022.botcommands.api.core.config.BApplicationConfigProps
import io.github.freya022.botcommands.api.core.config.DevConfig
import io.github.freya022.botcommands.api.core.config.application.cache.ApplicationCommandsCacheConfig
import io.github.freya022.botcommands.api.core.config.application.cache.ApplicationCommandsCacheConfigBuilder
import io.github.freya022.botcommands.internal.core.config.AbstractBotCommandsConfiguration
import io.github.freya022.botcommands.internal.core.config.ConfigurationValue
import net.dv8tion.jda.api.interactions.DiscordLocale
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.Name
import kotlin.io.path.Path

@ConditionalOnClass(BApplicationConfig::class)
@ConfigurationProperties(prefix = "botcommands.application", ignoreUnknownFields = true)
internal class BotCommandsApplicationCommandsConfiguration(
    @get:ConfigurationValue(
        path = "botcommands.application.enable",
        description = "Whether the application commands feature should be enabled. @RequiresApplicationCommands can be used to disable services when this feature is disabled.",
        defaultValue = "true",
    )
    val enable: Boolean = true,
    slashGuildIds: List<Long> = emptyList(),
    guildsToUpdate: List<Long> = emptyList(),
    override val testGuildIds: List<Long> = emptyList(),
    override val disableAutocompleteCache: Boolean = false,
    override val forceGuildCommands: Boolean = false,
    localizations: Map<String, List<DiscordLocale>> = emptyMap(),
    override val logMissingLocalizationKeys: Boolean = false,
    @param:Name("cache")
    internal val springCache: Cache = Cache(),
) : AbstractBotCommandsConfiguration(), BApplicationConfigProps {
    override val cache: Nothing get() = unusable()

    override val guildsToUpdate: List<Long> = when {
        guildsToUpdate.isNotEmpty() -> guildsToUpdate
        else -> slashGuildIds
    }

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
        @get:ConfigurationValue(
            "botcommands.application.cache.type",
            description = "Type of application command cache. See the documentation for more details.",
            defaultValue = "file",
        )
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

@OptIn(DevConfig::class)
internal fun BApplicationConfigBuilder.applyConfig(configuration: BotCommandsApplicationCommandsConfiguration) = apply {
    guildsToUpdate += configuration.guildsToUpdate
    testGuildIds += configuration.testGuildIds
    disableAutocompleteCache = configuration.disableAutocompleteCache
    configureCache(configuration)
    forceGuildCommands = configuration.forceGuildCommands
    configuration.baseNameToLocalesMap.forEach(::addLocalizations)
    logMissingLocalizationKeys = configuration.logMissingLocalizationKeys
}

@OptIn(DevConfig::class)
private fun BApplicationConfigBuilder.configureCache(configuration: BotCommandsApplicationCommandsConfiguration) {
    fun ApplicationCommandsCacheConfigBuilder.applyConfig(cache: BotCommandsApplicationCommandsConfiguration.Cache) {
        checkOnline = cache.checkOnline
        diffEngine = cache.diffEngine
        logDataIf = cache.logDataIf
    }
    when (configuration.springCache.type) {
        BotCommandsApplicationCommandsConfiguration.Cache.Type.FILE -> {
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
        BotCommandsApplicationCommandsConfiguration.Cache.Type.DATABASE -> databaseCache {
            applyConfig(configuration.springCache)
        }
        BotCommandsApplicationCommandsConfiguration.Cache.Type.NULL -> disableCache()
    }
}
