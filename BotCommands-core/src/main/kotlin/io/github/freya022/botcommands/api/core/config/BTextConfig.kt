package io.github.freya022.botcommands.api.core.config

import dev.freya02.jda.emojis.unicode.Emojis
import io.github.freya022.botcommands.api.commands.text.IHelpCommand
import io.github.freya022.botcommands.api.commands.text.TextPrefixSupplier
import io.github.freya022.botcommands.api.commands.text.annotations.RequiresTextCommands
import io.github.freya022.botcommands.api.core.messages.BotCommandsMessages
import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.api.core.utils.toImmutableList
import io.github.freya022.botcommands.internal.core.config.ConfigDSL
import io.github.freya022.botcommands.internal.core.config.ConfigurationValue
import net.dv8tion.jda.api.entities.emoji.Emoji

@InjectedService
interface BTextConfig : IConfig, BTextConfigProps {
    override val configType get() = BTextConfig::class.java
}

interface BTextConfigProps {
    /**
     * Whether the text commands feature should be enabled.
     *
     * You can use [@RequiresTextCommands][RequiresTextCommands]
     * to disable services when this is set to `false`.
     *
     * Default: `true`
     *
     * Spring property: `botcommands.text.enable`
     */
    @get:ConfigurationValue(
        path = "botcommands.text.enable",
        description = "Whether the text commands feature should be enabled.",
        defaultValue = "true",
    )
    val enable: Boolean

    /**
     * Whether the bot should look for commands when it is mentioned.
     *
     * This prefix is not always used for text command detection,
     * as it can be overridden by [TextPrefixSupplier], but you can read this property and return it
     * if, for example, the guild channel has no special prefix set.
     *
     * Default: `false`
     *
     * Spring property: `botcommands.text.usePingAsPrefix`
     */
    @get:ConfigurationValue(
        path = "botcommands.text.usePingAsPrefix",
        description = "Whether the bot should look for commands when it is mentioned. See the documentation for more details.",
        defaultValue = "false",
    )
    val usePingAsPrefix: Boolean

    /**
     * Prefixes the bot should listen to.
     *
     * These prefixes are not always used for text command detection,
     * as they can be overridden by [TextPrefixSupplier], but you can read this property and return them
     * if, for example, the guild channel has no special prefix set.
     *
     * Spring property: `botcommands.text.prefixes`
     */
    @get:ConfigurationValue(
        path = "botcommands.text.prefixes",
        description = "Prefixes the bot should listen to.",
    )
    val prefixes: List<String>

    /**
     * Whether the default help command is disabled. This also disables help content when a user misuses a command.
     *
     * This still lets you define your own help command with [IHelpCommand].
     *
     * Default: `false`
     *
     * Spring property: `botcommands.text.isHelpDisabled`
     */
    @get:ConfigurationValue(
        path = "botcommands.text.isHelpDisabled",
        description = "Whether the default help command is disabled. This also disables help content when a user misuses a command. You can still define your own [IHelpCommand].",
        defaultValue = "false",
    )
    val isHelpDisabled: Boolean

    /**
     * Whether command suggestions will be shown when a user tries to use an invalid command.
     *
     * Default: `true`
     *
     * Spring property: `botcommands.text.showSuggestions`
     */
    @get:ConfigurationValue(
        path = "botcommands.text.showSuggestions",
        description = "Whether command suggestions will be shown when a user tries to use an invalid command.",
        defaultValue = "true",
    )
    val showSuggestions: Boolean

    // 🐟 was also a strong candidate
    /**
     * Emoji used to indicate a user that their DMs are closed.
     *
     * This is only used if [the closed DMs error message][BotCommandsMessages.closedDirectMessages] can't be sent.
     *
     * Default: `mailbox_closed`
     *
     * Spring property: `botcommands.text.dmClosedEmoji`
     */
    @get:ConfigurationValue(
        path = "botcommands.text.dmClosedEmoji",
        description = "Emoji used to indicate a user that their DMs are closed, in the case where replying is not allowed either.",
        defaultValue = "mailbox_closed",
        type = "java.lang.String",
    )
    val dmClosedEmoji: Emoji
}

@ConfigDSL
class BTextConfigBuilder internal constructor() : BTextConfigProps {
    @set:JvmName("enable")
    override var enable: Boolean = true
    @set:JvmName("usePingAsPrefix")
    override var usePingAsPrefix: Boolean = false
    override val prefixes: MutableList<String> = mutableListOf()

    @set:JvmName("disableHelp")
    override var isHelpDisabled: Boolean = false
    @set:JvmName("showSuggestions")
    override var showSuggestions: Boolean = true

    var dmClosedEmojiSupplier: () -> Emoji = { Emojis.MAILBOX_CLOSED }
    override val dmClosedEmoji: Emoji get() = dmClosedEmojiSupplier()

    @JvmSynthetic
    internal fun build() = object : BTextConfig {
        override val enable = this@BTextConfigBuilder.enable
        override val usePingAsPrefix = this@BTextConfigBuilder.usePingAsPrefix
        override val prefixes = this@BTextConfigBuilder.prefixes.toImmutableList()
        override val isHelpDisabled = this@BTextConfigBuilder.isHelpDisabled
        override val showSuggestions = this@BTextConfigBuilder.showSuggestions
        override val dmClosedEmoji by lazy(this@BTextConfigBuilder.dmClosedEmojiSupplier)
    }
}
