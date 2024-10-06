package io.github.freya022.botcommands.api.core.options

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.Executable
import io.github.freya022.botcommands.api.parameters.AggregatedParameter
import kotlin.reflect.KParameter
import kotlin.reflect.KType

/**
 * One of the actual values in an [aggregated parameter][AggregatedParameter].
 */
interface Option {
    /**
     * The main context.
     */
    val context: BContext
        get() = executable.context

    /**
     * The executable this option is from.
     */
    val executable: Executable

    /**
     * A parameter from either the command function, or from an aggregation function.
     *
     * - If the aggregator is user-defined, then this parameter comes from the aggregation function.
     * - If the aggregator was created from a single option (this one),
     * then this parameter comes from the command function.
     * - In other cases, the parameter comes from an aggregation function
     *
     * This parameter is thus suitable for inspection.
     *
     * **Note:** An aggregator parameter may be referenced by multiple options (varargs, for example).
     */
    val kParameter: KParameter

    /**
     * Whether this option is part of [vararg parameter][AggregatedParameter.isVararg].
     *
     * If `true`, this option's value will be part of a [List].
     */
    val isVararg: Boolean

    /**
     * Whether this option's value can be omitted.
     *
     * See [default options](https://kotlinlang.org/docs/functions.html#default-arguments).
     */
    val isOptional: Boolean

    /**
     * Whether this option's value can be `null`.
     */
    val isNullable: Boolean

    /**
     * Whether this option's value can either be omitted or `null`
     *
     * **Note:** An option that can be omitted but non-null, cannot have `null` passed.
     */
    val isOptionalOrNullable: Boolean
        get() = isOptional || isNullable

    /**
     * Whether this option's value is required.
     *
     * This is effectively the opposite of [isOptionalOrNullable].
     */
    val isRequired: Boolean
        get() = !isOptionalOrNullable

    /**
     * The name of this option in the command/aggregator.
     *
     * **Note:** The name is not unique when this is part of a vararg aggregator.
     */
    val declaredName: String

    /**
     * The index of this option in the command/aggregator.
     *
     * **Note:** The index is not unique when this is part of a vararg aggregator.
     */
    val index: Int

    /**
     * Fallback value when this option's resolver returned `null`.
     */
    val nullValue: Any?

    /**
     * Generified type of the option.
     */
    val type: KType
}