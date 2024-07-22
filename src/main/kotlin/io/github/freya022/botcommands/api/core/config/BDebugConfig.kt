package io.github.freya022.botcommands.api.core.config

import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.internal.core.config.ConfigDSL
import io.github.freya022.botcommands.internal.core.config.ConfigurationValue

@InjectedService
interface BDebugConfig {
    /**
     * Whether the differences between old and new application commands data should be logged
     *
     * Default: `false`
     *
     * Spring property: `botcommands.debug.enableApplicationDiffsLogs`
     */
    @ConfigurationValue(path = "botcommands.debug.enableApplicationDiffsLogs", defaultValue = "false")
    val enableApplicationDiffsLogs: Boolean

    /**
     * Whether the missing localization strings when creation the command objects should be logged
     *
     * Default: `false`
     *
     * Spring property: `botcommands.debug.enabledMissingLocalizationLogs`
     */
    @ConfigurationValue(path = "botcommands.debug.enabledMissingLocalizationLogs", defaultValue = "false")
    val enabledMissingLocalizationLogs: Boolean
}

@ConfigDSL
class BDebugConfigBuilder internal constructor() : BDebugConfig {
    @set:JvmName("enableApplicationDiffsLogs")
    override var enableApplicationDiffsLogs: Boolean = false
    @set:JvmName("enabledMissingLocalizationLogs")
    override var enabledMissingLocalizationLogs: Boolean = false

    @JvmSynthetic
    internal fun build() = object : BDebugConfig {
        override val enableApplicationDiffsLogs = this@BDebugConfigBuilder.enableApplicationDiffsLogs
        override val enabledMissingLocalizationLogs = this@BDebugConfigBuilder.enabledMissingLocalizationLogs
    }
}
