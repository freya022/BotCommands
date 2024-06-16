package io.github.freya022.botcommands.api.commands

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.Executable
import io.github.freya022.botcommands.api.parameters.AggregatedParameter

/**
 * Represents a command parameter,
 * which has its value computed by an aggregation function, from one or more options.
 */
interface CommandParameter : AggregatedParameter {
    /**
     * The main context.
     */
    val context: BContext

    /**
     * The executable command this parameter is from
     */
    val command: Executable
}