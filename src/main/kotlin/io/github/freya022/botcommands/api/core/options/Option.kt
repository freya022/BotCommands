package io.github.freya022.botcommands.api.core.options

import kotlin.reflect.KParameter
import kotlin.reflect.KType

interface Option {
    /**
     * Might be a parameter from either the command function, or from an aggregator function.
     *
     * - If the aggregator was created from a single option (this one),
     * then this parameter comes from the command function.
     * - If the aggregator is user-defined, then this parameter comes from the aggregator function.
     *
     * This parameter is thus suitable for inspection.
     *
     * **Note:** An aggregator parameter may be referenced by multiple options (varargs, for example).
     */
    val kParameter: KParameter

    val isVararg: Boolean

    /**
     * `true` if the option can be omitted
     */
    val isOptional: Boolean

    /**
     * `true` if a `null` value is permitted by the command function/aggregator
     */
    val isNullable: Boolean

    /**
     * `true` is the option can be omitted or accepts a `null` value
     */
    val isOptionalOrNullable: Boolean
        get() = isOptional || isNullable
    val isRequired: Boolean
        get() = !isOptionalOrNullable
    val declaredName: String
    val index: Int
    val nullValue: Any?
    val type: KType
}