package com.freya02.botcommands.api.commands.prefixed

import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.prefixed.builder.TopLevelTextCommandBuilder
import com.freya02.botcommands.api.core.config.BConfigBuilder
import com.freya02.botcommands.internal.commands.application.SimpleCommandMap
import com.freya02.botcommands.internal.commands.prefixed.TopLevelTextCommandInfo
import com.freya02.botcommands.internal.core.BContextImpl

class TextCommandManager internal constructor(private val context: BContextImpl) {
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