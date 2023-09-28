package com.freya02.botcommands.api.core.config

import com.freya02.botcommands.api.core.service.annotations.InjectedService
import com.freya02.botcommands.api.core.utils.toImmutableList
import com.freya02.botcommands.internal.core.config.ConfigDSL

@InjectedService
interface BTextConfig {
    /**
     * Whether the bot should look for commands when it is mentioned.
     *
     * Default: `false`
     */
    val usePingAsPrefix: Boolean
    val prefixes: List<String>

    /**
     * Whether the default help command is disabled. This also disables help content when a user misuses a command.
     *
     * This still lets you define your own help command.
     *
     * Default: `false`
     */
    val isHelpDisabled: Boolean

    /**
     * Whether command suggestions will be shown when a user tries to use an invalid command.
     *
     * Default: `false`
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
    override var showSuggestions: Boolean = false

    @JvmSynthetic
    internal fun build() = object : BTextConfig {
        override val usePingAsPrefix = this@BTextConfigBuilder.usePingAsPrefix
        override val prefixes = this@BTextConfigBuilder.prefixes.toImmutableList()
        override val isHelpDisabled = this@BTextConfigBuilder.isHelpDisabled
        override val showSuggestions = this@BTextConfigBuilder.showSuggestions
    }
}