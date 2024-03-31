package io.github.freya022.botcommands.api.commands.text.provider

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.builder.setCallerAsDeclarationSite
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.CommandEvent
import io.github.freya022.botcommands.api.commands.text.IHelpCommand
import io.github.freya022.botcommands.api.commands.text.annotations.JDATextCommandVariation
import io.github.freya022.botcommands.api.commands.text.builder.TopLevelTextCommandBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.annotations.IgnoreStackFrame
import io.github.freya022.botcommands.internal.commands.application.NamedCommandMap
import io.github.freya022.botcommands.internal.commands.text.TopLevelTextCommandInfo

@IgnoreStackFrame
class TextCommandManager internal constructor(private val context: BContext) {
    internal val textCommands: NamedCommandMap<TopLevelTextCommandInfo> = NamedCommandMap()

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
     *  @see JDATextCommandVariation @JDATextCommandVariation
     */
    fun textCommand(name: String, builder: TopLevelTextCommandBuilder.() -> Unit) {
        TopLevelTextCommandBuilder(context, name)
            .setCallerAsDeclarationSite()
            .apply(builder)
            .build()
            .also(textCommands::putNewCommand)
    }
}