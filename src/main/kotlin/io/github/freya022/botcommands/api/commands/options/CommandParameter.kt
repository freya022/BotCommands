package io.github.freya022.botcommands.api.commands.options

import io.github.freya022.botcommands.api.core.Executable
import io.github.freya022.botcommands.api.parameters.AggregatedParameter

/**
 * Represents a command parameter,
 * which has its value computed by an aggregation function, from one or more options.
 */
interface CommandParameter : AggregatedParameter {
    /**
     * The executable command this parameter is from.
     */
    @Deprecated("Renamed to 'executable'", ReplaceWith("executable"))
    val command: Executable
        get() = executable
}