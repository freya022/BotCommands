package io.github.freya022.botcommands.api.core

import io.github.freya022.botcommands.api.core.options.Option
import io.github.freya022.botcommands.api.parameters.AggregatedParameter
import kotlin.reflect.KFunction

/**
 * Base class for any executable method (commands, components, modals...).
 */
interface Executable {
    /**
     * The target function of this executable.
     *
     * This is strictly for introspection purposes, do not call this function manually.
     */
    val function: KFunction<*>

    /**
     * The parameters of this executable.
     *
     * @see AggregatedParameter
     */
    val parameters: List<AggregatedParameter>

    /**
     * All options from this executable, including from its [aggregates][parameters].
     */
    val allOptions: List<Option>
        get() = parameters.flatMap { it.allOptions }

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