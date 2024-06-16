package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.core.Executable
import kotlin.reflect.KParameter
import kotlin.reflect.KType

/**
 * Parameter of an [Executable].
 *
 * @see AggregatedParameter
 */
interface MethodParameter {
    /**
     * The parameter of the function.
     */
    val kParameter: KParameter

    /**
     * Name of the parameter.
     */
    val name: String

    /**
     * Generified type of the parameter.
     */
    val type: KType

    /**
     * The index at which the parameter is, starts at 1 (#0 is the instance parameter).
     */
    val index: Int

    /**
     * Whether this parameter can be passed as `null` or be absent.
     */
    val isNullableOrOptional: Boolean

    /**
     * Whether this parameter is a **Java** primitive.
     */
    val isPrimitive: Boolean
}