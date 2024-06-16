package io.github.freya022.botcommands.api.commands

import io.github.freya022.botcommands.api.core.Executable
import io.github.freya022.botcommands.api.core.options.Option
import io.github.freya022.botcommands.api.parameters.AggregatedParameter

/**
 * Holds parameters of commands.
 */
interface ICommandParameterContainer : Executable {
    /**
     * Returns the aggregated parameter with the supplied *declared name* (i.e., name of the method parameter),
     * or `null` if not found.
     */
    fun getParameter(declaredName: String): AggregatedParameter?

    /**
     * Returns the option with the supplied *declared name* (i.e., name of the method parameter),
     * or `null` if not found.
     */
    fun getOptionByDeclaredName(name: String): Option? =
        parameters.flatMap { it.allOptions }.find { it.declaredName == name }
}