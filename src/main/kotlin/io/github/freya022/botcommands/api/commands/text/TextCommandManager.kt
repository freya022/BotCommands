package io.github.freya022.botcommands.api.commands.text

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.text.annotations.JDATextCommand
import io.github.freya022.botcommands.api.commands.text.builder.TopLevelTextCommandBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.application.SimpleCommandMap
import io.github.freya022.botcommands.internal.commands.prefixed.TopLevelTextCommandInfo

class TextCommandManager internal constructor(private val context: BContext) {
    @get:JvmSynthetic
    internal val textCommands: SimpleCommandMap<TopLevelTextCommandInfo> = SimpleCommandMap(null)

    /**
     * Declares the supplied function as a text command.
     *
     * ### Text command variations
     * A given text command path (such as `ban temp`) is composed of at least one variation;
     * Each variation has different parameters, and will display separately in the built-in help content.
     *
     * Each variation runs based off its declaration order,
     * the first variation that has a full match against the user input gets executed.
     *
     * If no regex-based variation (using a [BaseCommandEvent]) matches,
     * the fallback variation is executed (if a variation using [CommandEvent] exists).
     *
     * If no variation matches and there is no fallback,
     * then the [help content][IHelpCommand.onInvalidCommand] is invoked for the command.
     *
     * ### Requirements
     *  - The declaring class must be annotated with [@Command][Command].
     *  - First parameter must be [BaseCommandEvent], or, [CommandEvent] for fallback commands/manual token consumption
     *
     *  @see JDATextCommand @JDATextCommand
     */
    fun textCommand(name: String, builder: TopLevelTextCommandBuilder.() -> Unit) {
        TopLevelTextCommandBuilder(context, name)
            .apply(builder)
            .build()
            .also(textCommands::putNewCommand)
    }
}