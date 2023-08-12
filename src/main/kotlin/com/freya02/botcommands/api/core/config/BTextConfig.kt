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
}

@ConfigDSL
class BTextConfigBuilder internal constructor() : BTextConfig {
    override var usePingAsPrefix: Boolean = false
    override var prefixes: MutableList<String> = mutableListOf()

    override var isHelpDisabled: Boolean = false

    @JvmSynthetic
    internal fun build() = object : BTextConfig {
        override val usePingAsPrefix = this@BTextConfigBuilder.usePingAsPrefix
        override val prefixes = this@BTextConfigBuilder.prefixes.toImmutableList()
        override val isHelpDisabled = this@BTextConfigBuilder.isHelpDisabled
    }
}