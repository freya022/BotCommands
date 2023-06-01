package com.freya02.botcommands.api.core.config

import com.freya02.botcommands.api.commands.prefixed.TextCommandFilter
import com.freya02.botcommands.api.core.service.annotations.InjectedService
import com.freya02.botcommands.internal.toImmutableList
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel

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
     * Text command filters for the command listener to check on each **regular / regex** command
     *
     * If one of the filters returns `false`, then the command is not executed
     *
     * Command overloads are also not executed
     *
     * **Example: Restricting the bot to a certain [GuildMessageChannel]**
     * ```
     * CommandsBuilder.newBuilder()
     *      .textCommandBuilder(textCommandsBuilder -> textCommandsBuilder
     *          .addTextFilter(data -> data.event().getChannel().getIdLong() == 722891685755093076L)
     *      )
     * ```
     */
    val textFilters: List<TextCommandFilter>
}

class BTextConfigBuilder internal constructor() : BTextConfig {
    override var usePingAsPrefix: Boolean = false
    override var prefixes: MutableList<String> = mutableListOf()

    override var isHelpDisabled: Boolean = false
    override val textFilters: MutableList<TextCommandFilter> = mutableListOf()

    @JvmSynthetic
    internal fun build() = object : BTextConfig {
        override val usePingAsPrefix = this@BTextConfigBuilder.usePingAsPrefix
        override val prefixes = this@BTextConfigBuilder.prefixes.toImmutableList()
        override val isHelpDisabled = this@BTextConfigBuilder.isHelpDisabled
        override val textFilters = this@BTextConfigBuilder.textFilters.toImmutableList()
    }
}