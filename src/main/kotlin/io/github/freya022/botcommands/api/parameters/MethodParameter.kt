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
     * Whether this parameter's value can be omitted.
     *
     * See [default options](https://kotlinlang.org/docs/functions.html#default-arguments).
     */
    val isNullable: Boolean

    /**
     * Whether this parameter's value can be `null`.
     */
    val isOptional: Boolean

    /**
     * Whether this parameter's value can either be omitted or `null`
     *
     * **Note:** A parameter that can be omitted but non-null, cannot have `null` passed.
     */
    val isNullableOrOptional: Boolean
        get() = isNullable || isOptional
}