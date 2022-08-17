package com.freya02.botcommands.core.api.config

import com.freya02.botcommands.api.prefixed.HelpBuilderConsumer
import com.freya02.botcommands.api.prefixed.TextCommandFilter
import com.freya02.botcommands.core.api.annotations.LateService
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.GuildMessageChannel
import java.io.InputStream
import java.util.function.Supplier

@LateService
class BTextConfig internal constructor() {
    var usePingAsPrefix: Boolean = false

    val prefixes: MutableList<String> = mutableListOf()

    /**
     * Sets the default embed supplier, this lets you have an embed builder as a template, reducing boilerplate.
     *
     * This embed is also used in the help commands.
     */
    var defaultEmbedSupplier: Supplier<EmbedBuilder> = Supplier { EmbedBuilder() }

    /**
     * Sets the default footer icon for the [default embed supplier][defaultEmbedSupplier].
     *
     * **The icon name when used will be "icon.jpg", your icon must be a JPG file and be the same name**.
     */
    var defaultFooterIconSupplier = Supplier<InputStream?> { null }

    /**
     * Whether the default help command is disabled. This also disables help content when an user misuses a command.
     *
     * This still lets you define your own help command.
     */
    var isHelpDisabled: Boolean = false

    /**
     * A consumer that's called when an help embed is about to be sent.
     *
     * That embed can be for the command list as well as individual commands.
     */
    var helpBuilderConsumer: HelpBuilderConsumer? = null

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