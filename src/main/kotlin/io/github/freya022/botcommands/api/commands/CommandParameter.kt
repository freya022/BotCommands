package io.github.freya022.botcommands.api.commands

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.Executable
import io.github.freya022.botcommands.api.parameters.AggregatedParameter

interface CommandParameter : AggregatedParameter {
    val context: BContext
    val command: Executable
}