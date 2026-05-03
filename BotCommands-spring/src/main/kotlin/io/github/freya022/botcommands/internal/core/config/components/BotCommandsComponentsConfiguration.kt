package io.github.freya022.botcommands.internal.core.config.components

import io.github.freya022.botcommands.api.core.config.BComponentsConfigBuilder
import io.github.freya022.botcommands.api.core.config.BComponentsConfigProps
import io.github.freya022.botcommands.internal.core.config.AbstractBotCommandsConfiguration
import io.github.freya022.botcommands.internal.core.config.ConfigurationValue
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.context.properties.ConfigurationProperties

@ConditionalOnClass(BComponentsConfigBuilder::class)
@ConfigurationProperties(prefix = "botcommands.components", ignoreUnknownFields = false)
internal class BotCommandsComponentsConfiguration(
    @get:ConfigurationValue(
        path = "botcommands.components.enable",
        description = "Whether the components feature should be enabled. Enabling this requires a [ConnectionSupplier] service. @RequiresComponents can be used to disable services when this feature is disabled.",
        defaultValue = "true",
    )
    val enable: Boolean = true
) : AbstractBotCommandsConfiguration(), BComponentsConfigProps

internal fun BComponentsConfigBuilder.applyConfig(configuration: BotCommandsComponentsConfiguration) = apply {

}
