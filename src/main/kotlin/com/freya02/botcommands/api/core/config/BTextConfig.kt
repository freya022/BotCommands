package com.freya02.botcommands.api.core.config

import com.freya02.botcommands.api.commands.prefixed.TextCommandFilter
import com.freya02.botcommands.api.core.annotations.InjectedService
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel

@InjectedService
class BTextConfig internal constructor() {
    var usePingAsPrefix: Boolean = false

    val prefixes: MutableList<String> = mutableListOf()

    /**
     * Whether the default help command is disabled. This also disables help content when an user misuses a command.
     *
     * This still lets you define your own help command.
     */
    var isHelpDisabled: Boolean = false

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
    val textFilters: MutableList<TextCommandFilter> = mutableListOf()
}