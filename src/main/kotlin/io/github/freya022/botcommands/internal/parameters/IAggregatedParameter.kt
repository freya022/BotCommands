package io.github.freya022.botcommands.internal.parameters

import io.github.freya022.botcommands.internal.core.options.Option
import io.github.freya022.botcommands.internal.core.reflection.AggregatorFunction

interface IAggregatedParameter : MethodParameter {
    val aggregator: AggregatorFunction

    val options: List<Option>

    val nestedAggregatedParameters: List<IAggregatedParameter>

    val allOptions: List<Option>
        get() = options + nestedAggregatedParameters.flatMap { it.allOptions }
}