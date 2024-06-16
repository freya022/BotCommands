package io.github.freya022.botcommands.api.core

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
}