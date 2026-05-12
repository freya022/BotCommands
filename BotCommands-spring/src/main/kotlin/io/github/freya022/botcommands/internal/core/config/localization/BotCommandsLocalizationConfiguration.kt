package io.github.freya022.botcommands.internal.core.config.localization

import io.github.freya022.botcommands.api.core.config.BLocalizationConfigBuilder
import io.github.freya022.botcommands.api.core.config.BLocalizationConfigProps
import io.github.freya022.botcommands.internal.core.config.AbstractBotCommandsConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import kotlin.collections.plusAssign

// This is always on, not really a module but still has a config
@ConfigurationProperties(prefix = "botcommands.localization", ignoreUnknownFields = false)
internal class BotCommandsLocalizationConfiguration(
    override val responseBundles: Set<String> = emptySet(),
) : AbstractBotCommandsConfiguration(), BLocalizationConfigProps

internal fun BLocalizationConfigBuilder.applyConfig(configuration: BotCommandsLocalizationConfiguration) = apply {
    responseBundles += configuration.responseBundles
}
