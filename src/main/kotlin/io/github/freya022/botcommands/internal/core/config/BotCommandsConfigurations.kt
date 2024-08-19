@file:Suppress("ConfigurationProperties")

package io.github.freya022.botcommands.internal.core.config

import io.github.freya022.botcommands.api.commands.application.diff.DiffEngine
import io.github.freya022.botcommands.api.core.config.*
import io.github.freya022.botcommands.api.utils.EmojiUtils
import io.github.freya022.botcommands.internal.utils.throwArgument
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.requests.GatewayIntent
import org.springframework.boot.context.properties.ConfigurationProperties
import java.nio.file.Path
import kotlin.time.Duration
import kotlin.time.toKotlinDuration
import java.time.Duration as JavaDuration

@ConfigurationProperties(prefix = "botcommands.core", ignoreUnknownFields = false)
internal class BotCommandsCoreConfiguration(
    override val predefinedOwnerIds: Set<Long> = emptySet(),
    override val packages: Set<String> = emptySet(),
    override val classes: Set<Class<*>> = emptySet(),
    override val disableExceptionsInDMs: Boolean = false,
    @Suppress("OVERRIDE_DEPRECATION")
    override val disableAutocompleteCache: Boolean = false,
    override val ignoredIntents: Set<GatewayIntent> = emptySet(),
    override val ignoredEventIntents: Set<Class<out Event>> = emptySet(),
) : BConfig {
    @Suppress("OVERRIDE_DEPRECATION")
    override val ownerIds: Set<Long> get() = predefinedOwnerIds
    override val classGraphProcessors: Nothing get() = unusable()
    override val debugConfig: Nothing get() = unusable()
    override val serviceConfig: Nothing get() = unusable()
    override val databaseConfig: Nothing get() = unusable()
    override val localizationConfig: Nothing get() = unusable()
    override val textConfig: Nothing get() = unusable()
    override val applicationConfig: Nothing get() = unusable()
    override val componentsConfig: Nothing get() = unusable()
    override val coroutineScopesConfig: Nothing get() = unusable()
}

@OptIn(DevConfig::class)
internal fun BConfigBuilder.applyConfig(configuration: BotCommandsCoreConfiguration) = apply {
    predefinedOwnerIds += configuration.predefinedOwnerIds
    packages += configuration.packages
    classes += configuration.classes
    disableExceptionsInDMs = configuration.disableExceptionsInDMs
    @Suppress("DEPRECATION")
    disableAutocompleteCache = configuration.disableAutocompleteCache
    ignoredIntents += configuration.ignoredIntents
    ignoredEventIntents += configuration.ignoredEventIntents
}

@Suppress("DEPRECATION")
@ConfigurationProperties(prefix = "botcommands.debug", ignoreUnknownFields = false)
internal class BotCommandsDebugConfiguration(
    @Suppress("OVERRIDE_DEPRECATION")
    override val enableApplicationDiffsLogs: Boolean = false,
    @Suppress("OVERRIDE_DEPRECATION")
    override val enabledMissingLocalizationLogs: Boolean = false
) : BDebugConfig

@Suppress("DEPRECATION")
internal fun BDebugConfigBuilder.applyConfig(configuration: BotCommandsDebugConfiguration) = apply {
    enableApplicationDiffsLogs = configuration.enableApplicationDiffsLogs
    enabledMissingLocalizationLogs = configuration.enabledMissingLocalizationLogs
}

@ConfigurationProperties(prefix = "botcommands.service", ignoreUnknownFields = false)
internal class BotCommandsServiceConfiguration : BServiceConfig {
    @Deprecated("For removal, didn't do much in the first place")
    override val serviceAnnotations: Nothing get() = unusable()
    override val instanceSupplierMap: Nothing get() = unusable()
}

internal fun BServiceConfigBuilder.applyConfig(configuration: BotCommandsServiceConfiguration) = apply {

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
    dmClosedEmoji: String? = null
) : BTextConfig {
    override val dmClosedEmoji = EmojiUtils.resolveJDAEmoji(dmClosedEmoji ?: "mailbox_closed")
}

internal fun BTextConfigBuilder.applyConfig(configuration: BotCommandsTextConfiguration) = apply {
    enable = configuration.enable
    usePingAsPrefix = configuration.usePingAsPrefix
    prefixes += configuration.prefixes
    isHelpDisabled = configuration.isHelpDisabled
    showSuggestions = configuration.showSuggestions
    dmClosedEmoji = configuration.dmClosedEmoji
}

@ConfigurationProperties(prefix = "botcommands.localization", ignoreUnknownFields = false)
internal class BotCommandsLocalizationConfiguration(
    override val responseBundles: Set<String> = emptySet(),
) : BLocalizationConfig

internal fun BLocalizationConfigBuilder.applyConfig(configuration: BotCommandsLocalizationConfiguration) = apply {
    responseBundles += configuration.responseBundles
}

@ConfigurationProperties(prefix = "botcommands.application", ignoreUnknownFields = false)
internal class BotCommandsApplicationConfiguration(
    override val enable: Boolean = true,
    override val slashGuildIds: List<Long> = emptyList(),
    override val testGuildIds: List<Long> = emptyList(),
    override val disableAutocompleteCache: Boolean = false,
    override val enableCaching: Boolean = true,
    override val commandCachePath: Path? = null,
    override val onlineAppCommandCheckEnabled: Boolean = false,
    override val diffEngine: DiffEngine = DiffEngine.NEW,
    override val logApplicationCommandData: Boolean = false,
    override val forceGuildCommands: Boolean = false,
    localizations: Map<String, List<DiscordLocale>> = emptyMap(),
    override val logMissingLocalizationKeys: Boolean = false,
) : BApplicationConfig {
    override val baseNameToLocalesMap = localizations
}

@OptIn(DevConfig::class)
internal fun BApplicationConfigBuilder.applyConfig(configuration: BotCommandsApplicationConfiguration) = apply {
    enable = configuration.enable
    slashGuildIds += configuration.slashGuildIds
    testGuildIds += configuration.testGuildIds
    disableAutocompleteCache = configuration.disableAutocompleteCache
    enableCaching = configuration.enableCaching
    commandCachePath = configuration.commandCachePath
    onlineAppCommandCheckEnabled = configuration.onlineAppCommandCheckEnabled
    diffEngine = configuration.diffEngine
    logApplicationCommandData = configuration.logApplicationCommandData
    forceGuildCommands = configuration.forceGuildCommands
    configuration.baseNameToLocalesMap.forEach(::addLocalizations)
    logMissingLocalizationKeys = configuration.logMissingLocalizationKeys
}

@ConfigurationProperties(prefix = "botcommands.components", ignoreUnknownFields = false)
internal class BotCommandsComponentsConfiguration(
    override val enable: Boolean = false
) : BComponentsConfig

internal fun BComponentsConfigBuilder.applyConfig(configuration: BotCommandsComponentsConfiguration) = apply {
    enable = configuration.enable
}

private fun unusable(): Nothing = throwArgument("Cannot be used")
