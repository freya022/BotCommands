package io.github.freya022.botcommands.internal.core.config.emojis

import io.github.freya022.botcommands.api.core.config.BAppEmojisConfig
import io.github.freya022.botcommands.api.core.config.BAppEmojisConfigBuilder
import io.github.freya022.botcommands.api.core.config.BAppEmojisConfigProps
import io.github.freya022.botcommands.internal.core.config.AbstractBotCommandsConfiguration
import io.github.freya022.botcommands.internal.core.config.ConfigurationValue
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.context.properties.ConfigurationProperties

@ConditionalOnClass(BAppEmojisConfig::class)
@ConfigurationProperties(prefix = "botcommands.app.emojis", ignoreUnknownFields = false)
internal class BotCommandsAppEmojisConfiguration(
    @get:ConfigurationValue(
        path = "botcommands.app.emojis.enable",
        description = "Allows uploading application emojis at startup, and retrieving them from [AppEmojisRegistry].",
        defaultValue = "false",
    )
    val enable: Boolean = true,
    override val deleteOnOutOfSlots: Boolean = false,
) : AbstractBotCommandsConfiguration(), BAppEmojisConfigProps {

}

internal fun BAppEmojisConfigBuilder.applyConfig(configuration: BotCommandsAppEmojisConfiguration) = apply {
    deleteOnOutOfSlots = configuration.deleteOnOutOfSlots
}
