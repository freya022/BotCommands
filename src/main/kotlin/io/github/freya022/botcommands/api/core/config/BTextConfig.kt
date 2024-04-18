package io.github.freya022.botcommands.api.core.config

import io.github.freya022.botcommands.api.commands.text.IHelpCommand
import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.api.core.utils.toImmutableList
import io.github.freya022.botcommands.internal.core.config.ConfigDSL

@InjectedService
interface BTextConfig {
    /**
     * Whether the bot should look for commands when it is mentioned.
     *
     * Default: `false`
     *
     * Spring property: `botcommands.text.usePingAsPrefix`
     */
    val usePingAsPrefix: Boolean

    /**
     * Prefixes the bot should listen to.
     *
     * Spring property: `botcommands.text.prefixes`
     */
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
    val isHelpDisabled: Boolean

    /**
     * Whether command suggestions will be shown when a user tries to use an invalid command.
     *
     * Default: `true`
     *
     * Spring property: `botcommands.text.showSuggestions`
     */
    val showSuggestions: Boolean
}

@ConfigDSL
class BTextConfigBuilder internal constructor() : BTextConfig {
    @set:JvmName("usePingAsPrefix")
    override var usePingAsPrefix: Boolean = false
    override val prefixes: MutableList<String> = mutableListOf()

    @set:JvmName("disableHelp")
    override var isHelpDisabled: Boolean = false
    @set:JvmName("showSuggestions")
    override var showSuggestions: Boolean = true

    @JvmSynthetic
    internal fun build() = object : BTextConfig {
        override val usePingAsPrefix = this@BTextConfigBuilder.usePingAsPrefix
        override val prefixes = this@BTextConfigBuilder.prefixes.toImmutableList()
        override val isHelpDisabled = this@BTextConfigBuilder.isHelpDisabled
        override val showSuggestions = this@BTextConfigBuilder.showSuggestions
    }
}