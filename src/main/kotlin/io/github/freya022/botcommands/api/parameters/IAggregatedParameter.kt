package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.core.options.Option

interface IAggregatedParameter : MethodParameter {
    val isVararg: Boolean

    val options: List<Option>

    val nestedAggregatedParameters: List<IAggregatedParameter>

    val allOptions: List<Option>
        get() = options + nestedAggregatedParameters.flatMap { it.allOptions }
}