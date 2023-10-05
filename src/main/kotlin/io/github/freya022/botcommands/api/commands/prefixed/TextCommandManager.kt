package io.github.freya022.botcommands.api.commands.prefixed

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.prefixed.builder.TopLevelTextCommandBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.internal.commands.application.SimpleCommandMap
import io.github.freya022.botcommands.internal.commands.prefixed.TopLevelTextCommandInfo

class TextCommandManager internal constructor(private val context: BContext) {
    @get:JvmSynthetic
    internal val textCommands: SimpleCommandMap<TopLevelTextCommandInfo> = SimpleCommandMap(null)

    /**
     * Declares the supplied function as a text command.
     *
     * Text commands are composed of "variations";
     * functions with the same path form a group of variations.<br>
     * Each variation is run based off the order you declare them in,
     * the first variation that has its syntax match against the user input gets executed.
     *
     * **Requirements:**
     *  - The declaring class must be annotated with [@Command][Command]
     *  - The method must be in the [search path][BConfigBuilder.addSearchPath]
     *  - First parameter must be [BaseCommandEvent], or, [CommandEvent] for fallback commands/manual token consumption
     */
    fun textCommand(name: String, builder: TopLevelTextCommandBuilder.() -> Unit) {
        TopLevelTextCommandBuilder(context, name)
            .apply(builder)
            .build()
            .also(textCommands::putNewCommand)
    }
}