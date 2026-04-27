package io.github.freya022.botcommands.internal.core.config.commands.text

import io.github.freya022.botcommands.api.core.config.BTextConfig
import io.github.freya022.botcommands.api.core.config.BTextConfigBuilder
import io.github.freya022.botcommands.api.core.config.BTextConfigProps
import io.github.freya022.botcommands.api.utils.EmojiUtils
import io.github.freya022.botcommands.internal.core.config.AbstractBotCommandsConfiguration
import io.github.freya022.botcommands.internal.core.config.ConfigurationValue
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.Name

@ConditionalOnClass(BTextConfig::class)
@ConfigurationProperties(prefix = "botcommands.text", ignoreUnknownFields = true)
internal class BotCommandsTextCommandsConfiguration(
    @get:ConfigurationValue(
        path = "botcommands.text.enable",
        description = "Whether the text commands feature should be enabled. @RequiresTextCommands can be used to disable services when this feature is disabled.",
        defaultValue = "true",
    )
    val enable: Boolean = true,
    override val usePingAsPrefix: Boolean = false,
    override val prefixes: List<String> = emptyList(),
    override val isHelpDisabled: Boolean = false,
    override val showSuggestions: Boolean = true,
    @param:Name("dmClosedEmoji")
    internal val dmClosedEmojiString: String? = null
) : AbstractBotCommandsConfiguration(), BTextConfigProps {
    override val dmClosedEmoji: Nothing get() = unusable()
}

internal fun BTextConfigBuilder.applyConfig(configuration: BotCommandsTextCommandsConfiguration) = apply {
    usePingAsPrefix = configuration.usePingAsPrefix
    prefixes += configuration.prefixes
    isHelpDisabled = configuration.isHelpDisabled
    showSuggestions = configuration.showSuggestions
    configuration.dmClosedEmojiString?.let { dmClosedEmojiSupplier = { EmojiUtils.resolveJDAEmoji(it) } }
}
