package io.github.freya022.botcommands.internal.components.timeout

import io.github.freya022.botcommands.internal.CommandOptions
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.options.builder.OptionAggregateBuilderImpl
import io.github.freya022.botcommands.internal.core.reflection.toAggregatorFunction
import io.github.freya022.botcommands.internal.parameters.AbstractMethodParameter
import io.github.freya022.botcommands.internal.parameters.AggregatedParameterMixin
import io.github.freya022.botcommands.internal.transform
import kotlin.reflect.KClass

internal class TimeoutHandlerParameter internal constructor(
    context: BContextImpl,
    aggregateBuilder: OptionAggregateBuilderImpl<*>,
    aggregatorFirstParamType: KClass<*>
) : AbstractMethodParameter(aggregateBuilder.parameter),
    AggregatedParameterMixin {

    override val aggregator = aggregateBuilder.aggregator.toAggregatorFunction(context, aggregatorFirstParamType)

    override val nestedAggregatedParameters = aggregateBuilder.optionAggregateBuilders.transform {
        TimeoutHandlerParameter(context, it as OptionAggregateBuilderImpl<*>, aggregatorFirstParamType)
    }

    override val options = CommandOptions.transform(
        context,
        null,
        aggregateBuilder,
        optionFinalizer = ::TimeoutHandlerOption
    )
}