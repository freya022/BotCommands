package io.github.freya022.botcommands.internal.core.config.modals

import io.github.freya022.botcommands.api.core.config.BModalsConfigBuilder
import io.github.freya022.botcommands.api.core.config.BModalsConfigProps
import io.github.freya022.botcommands.internal.core.config.AbstractBotCommandsConfiguration
import io.github.freya022.botcommands.internal.core.config.ConfigurationValue
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.context.properties.ConfigurationProperties

@ConditionalOnClass(BModalsConfigBuilder::class)
@ConfigurationProperties(prefix = "botcommands.modals", ignoreUnknownFields = true)
internal class BotCommandsModalsConfiguration(
    @get:ConfigurationValue(
        path = "botcommands.modals.enable",
        description = "Whether the modals feature should be enabled. @RequiresModals can be used to disable services when this feature is disabled.",
        defaultValue = "true",
    )
    val enable: Boolean = true,
) : AbstractBotCommandsConfiguration(), BModalsConfigProps {

}

internal fun BModalsConfigBuilder.applyConfig(configuration: BotCommandsModalsConfiguration) = apply {

}
