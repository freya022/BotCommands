package io.github.freya022.botcommands.api.core.config

import dev.freya02.jda.emojis.unicode.Emojis
import io.github.freya022.botcommands.api.commands.text.IHelpCommand
import io.github.freya022.botcommands.api.commands.text.TextPrefixSupplier
import io.github.freya022.botcommands.api.commands.text.annotations.RequiresTextCommands
import io.github.freya022.botcommands.api.commands.text.messages.TextCommandsMessages
import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.api.core.utils.toImmutableList
import io.github.freya022.botcommands.internal.core.config.ConfigDSL
import io.github.freya022.botcommands.internal.core.config.ConfigurationValue
import net.dv8tion.jda.api.entities.emoji.Emoji

/**
 * Configuration for the text commands feature.
 *
 * A configuration of this feature must be registered for it to be active.
 *
 * Spring users can set the `botcommands.text.enable` property to `false` to disable this feature,
 * as adding the dependency will enable it by default.
 *
 * [@RequiresTextCommands][RequiresTextCommands] can be used to disable services when this feature isn't registered.
 *
 * @see [BTextConfig.builder]
 * @see [registerTextCommands]
 */
@InjectedService
interface BTextConfig : IConfig, BTextConfigProps {
    override val configType get() = BTextConfig::class.java

    companion object {
        /**
         * Creates a new [BTextConfigBuilder], you must [build][BTextConfigBuilder.build] it and [register][BConfigBuilder.registerModule] it.
         */
        @JvmStatic
        fun builder(): BTextConfigBuilder {
            return BTextConfigBuilder.create()
        }
    }
}

interface BTextConfigProps {
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
     * This is only used if [the closed DMs error message][TextCommandsMessages.closedDirectMessages] can't be sent.
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

/**
 * Builder of [BTextConfig].
 *
 * @see BTextConfig.builder
 */
@ConfigDSL
class BTextConfigBuilder internal constructor() : BTextConfigProps {
    @set:JvmName("usePingAsPrefix")
    override var usePingAsPrefix: Boolean = false
    override val prefixes: MutableList<String> = mutableListOf()

    @set:JvmName("disableHelp")
    override var isHelpDisabled: Boolean = false
    @set:JvmName("showSuggestions")
    override var showSuggestions: Boolean = true

    var dmClosedEmojiSupplier: () -> Emoji = { Emojis.MAILBOX_CLOSED }
    override val dmClosedEmoji: Emoji get() = dmClosedEmojiSupplier()

    fun build() = object : BTextConfig {
        override val usePingAsPrefix = this@BTextConfigBuilder.usePingAsPrefix
        override val prefixes = this@BTextConfigBuilder.prefixes.toImmutableList()
        override val isHelpDisabled = this@BTextConfigBuilder.isHelpDisabled
        override val showSuggestions = this@BTextConfigBuilder.showSuggestions
        override val dmClosedEmoji by lazy(this@BTextConfigBuilder.dmClosedEmojiSupplier)
    }

    internal companion object {
        @JvmSynthetic
        internal fun create(): BTextConfigBuilder = BTextConfigBuilder()
    }
}

/**
 * Registers the text commands feature.
 *
 * @param block A block for further configuration
 *
 * @see BTextConfig
 */
fun BConfigBuilder.registerTextCommands(block: BTextConfigBuilder.() -> Unit = { }) {
    val config = BTextConfigBuilder.create()
        .apply(block)
        .build()
    registerModule(config)
}
