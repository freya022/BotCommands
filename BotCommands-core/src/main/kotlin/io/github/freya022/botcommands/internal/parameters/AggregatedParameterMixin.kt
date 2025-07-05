package io.github.freya022.botcommands.internal.parameters

import io.github.freya022.botcommands.api.parameters.AggregatedParameter
import io.github.freya022.botcommands.internal.core.options.OptionImpl
import io.github.freya022.botcommands.internal.core.options.builder.InternalAggregators.isVarargAggregator
import io.github.freya022.botcommands.internal.core.reflection.AggregatorFunction

internal interface AggregatedParameterMixin : AggregatedParameter, MethodParameterMixin {
    val aggregator: AggregatorFunction
    override val isVararg: Boolean
        get() = aggregator.aggregator.isVarargAggregator()

    override val options: List<OptionImpl>

    override val nestedAggregatedParameters: List<AggregatedParameterMixin>

    override val allOptions: List<OptionImpl>
        get() = options + nestedAggregatedParameters.flatMap { it.allOptions }
}