@file:Suppress("ConfigurationProperties")

package io.github.freya022.botcommands.internal.core.config

import io.github.freya022.botcommands.api.core.config.*
import io.github.freya022.botcommands.api.utils.EmojiUtils
import io.github.freya022.botcommands.internal.utils.throwArgument
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.requests.GatewayIntent
import org.springframework.boot.context.properties.ConfigurationProperties
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

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

@ConfigurationProperties(prefix = "botcommands.debug", ignoreUnknownFields = false)
internal class BotCommandsDebugConfiguration(
    override val enableApplicationDiffsLogs: Boolean = false,
    override val enabledMissingLocalizationLogs: Boolean = false
) : BDebugConfig

internal fun BDebugConfigBuilder.applyConfig(configuration: BotCommandsDebugConfiguration) = apply {
    enableApplicationDiffsLogs = configuration.enableApplicationDiffsLogs
    enabledMissingLocalizationLogs = configuration.enabledMissingLocalizationLogs
}

@ConfigurationProperties(prefix = "botcommands.service", ignoreUnknownFields = false)
internal class BotCommandsServiceConfiguration : BServiceConfig {
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
    queryLogThresholdMillis: Long? = null
) : BDatabaseConfig {
    override val queryLogThreshold: Duration = queryLogThresholdMillis?.milliseconds ?: Duration.INFINITE
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
    override val usePingAsPrefix: Boolean = false,
    override val prefixes: List<String> = emptyList(),
    override val isHelpDisabled: Boolean = false,
    override val showSuggestions: Boolean = true,
    override val dmClosedEmoji: Emoji = EmojiUtils.resolveJDAEmoji("mailbox_closed")
) : BTextConfig

internal fun BTextConfigBuilder.applyConfig(configuration: BotCommandsTextConfiguration) = apply {
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
    override val slashGuildIds: List<Long> = emptyList(),
    override val testGuildIds: List<Long> = emptyList(),
    override val disableAutocompleteCache: Boolean = false,
    override val onlineAppCommandCheckEnabled: Boolean = false,
    override val forceGuildCommands: Boolean = false,
    localizations: Map<String, List<DiscordLocale>> = emptyMap()
) : BApplicationConfig {
    override val baseNameToLocalesMap = localizations
}

@OptIn(DevConfig::class)
internal fun BApplicationConfigBuilder.applyConfig(configuration: BotCommandsApplicationConfiguration) = apply {
    slashGuildIds += configuration.slashGuildIds
    testGuildIds += configuration.testGuildIds
    disableAutocompleteCache = configuration.disableAutocompleteCache
    onlineAppCommandCheckEnabled = configuration.onlineAppCommandCheckEnabled
    forceGuildCommands = configuration.forceGuildCommands
    configuration.baseNameToLocalesMap.forEach(::addLocalizations)
}

@ConfigurationProperties(prefix = "botcommands.components", ignoreUnknownFields = false)
internal class BotCommandsComponentsConfiguration(
    val enable: Boolean = false
) : BComponentsConfig {
    override val useComponents: Boolean get() = enable
}

internal fun BComponentsConfigBuilder.applyConfig(configuration: BotCommandsComponentsConfiguration) = apply {
    useComponents = configuration.useComponents
}

private fun unusable(): Nothing = throwArgument("Cannot be used")
