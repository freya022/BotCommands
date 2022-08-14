package com.freya02.botcommands.core.api.config

import com.freya02.botcommands.api.prefixed.TextCommandFilter
import com.freya02.botcommands.core.api.annotations.LateService
import net.dv8tion.jda.api.entities.GuildMessageChannel

@LateService
class BTextConfig internal constructor() {
    var usePingAsPrefix: Boolean = false

    val prefixes: MutableList<String> = mutableListOf()

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