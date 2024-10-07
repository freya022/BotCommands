package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.core.options.Option
import io.github.freya022.botcommands.internal.utils.throwInternal

/**
 * Parameter which has its value computed by an aggregation function, from one or more options.
 */
interface AggregatedParameter : MethodParameter {
    /**
     * Whether this parameter is computed by an aggregator collecting an undefined amount parameters.
     *
     * If `true`, this parameter is a [List].
     */
    val isVararg: Boolean

    /**
     * Options consumed by this parameter's aggregator.
     */
    val options: List<Option>

    /**
     * Aggregations that this parameter also depends on.
     */
    val nestedAggregatedParameters: List<AggregatedParameter>

    /**
     * Options from this parameter and also [nested aggregated parameters][nestedAggregatedParameters].
     *
     * These options have no specific order of appearance, use [allOptionsOrdered] instead.
     */
    val allOptions: List<Option>
        get() = options + nestedAggregatedParameters.flatMap { it.allOptions }

    /**
     * Options from this parameter and also [nested aggregated parameters][nestedAggregatedParameters],
     * sorted by order of appearance in this function.
     */
    val allOptionsOrdered: List<Option>
        get() = buildList {
            val sorted = (options + nestedAggregatedParameters).sortedBy {
                when (it) {
                    is Option -> it.index
                    is AggregatedParameter -> it.index
                    else -> throwInternal("Unhandled type ${it.javaClass.name}")
                }
            }

            for (any in sorted) {
                when (any) {
                    is Option -> add(any)
                    is AggregatedParameter -> addAll(any.allOptionsOrdered)
                    else -> throwInternal("Unhandled type ${any.javaClass.name}")
                }
            }
        }

    /**
     * Returns the option with the supplied *declared name* (i.e., name of the method parameter),
     * or `null` if not found.
     *
     * This does not take into account [nested aggregations][nestedAggregatedParameters],
     * use [getNestedOptionByDeclaredName] instead.
     */
    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated("For removal, prefer using collection operations on 'options' instead, make an extension or an utility method")
    fun getOptionByDeclaredName(name: String): Option? =
        options.find { it.declaredName == name }

    /**
     * Returns the option with the supplied *declared name* (i.e., name of the method parameter),
     * or `null` if not found.
     *
     * Takes into account [nested aggregations][nestedAggregatedParameters].
     */
    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated("For removal, there can be one or more options with the provided name, " +
            "prefer using collection operations on 'allOptions' instead, make an extension or an utility method")
    fun getNestedOptionByDeclaredName(name: String): Option? =
        allOptions.find { it.declaredName == name }
}