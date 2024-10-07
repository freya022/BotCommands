package io.github.freya022.botcommands.api.core

import io.github.freya022.botcommands.api.core.options.Option
import io.github.freya022.botcommands.api.parameters.AggregatedParameter
import kotlin.reflect.KFunction

/**
 * Base class for any executable method (commands, components, modals...).
 *
 * This never represents an aggregator.
 */
interface Executable {
    /**
     * The main context.
     */
    val context: BContext

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
    @Deprecated("For removal, confusing on whether it searches nested parameters, " +
            "prefer using collection operations on 'parameters' instead, make an extension or an utility method")
    fun getParameter(declaredName: String): AggregatedParameter?

    /**
     * Returns the option with the supplied *declared name* (i.e., name of the method parameter),
     * or `null` if not found.
     */
    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated("For removal, there can be one or more options with the provided name, " +
            "prefer using collection operations on 'allOptions' instead, make an extension or an utility method")
    fun getOptionByDeclaredName(name: String): Option? =
        allOptions.find { it.declaredName == name }
}