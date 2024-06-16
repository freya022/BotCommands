package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.core.options.Option

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
     */
    val allOptions: List<Option>
        get() = options + nestedAggregatedParameters.flatMap { it.allOptions }
}