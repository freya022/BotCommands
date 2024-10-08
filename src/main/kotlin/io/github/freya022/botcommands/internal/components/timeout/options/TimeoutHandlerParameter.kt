package io.github.freya022.botcommands.internal.components.timeout.options

import io.github.freya022.botcommands.internal.components.timeout.TimeoutDescriptor
import io.github.freya022.botcommands.internal.core.options.builder.OptionAggregateBuilderImpl
import io.github.freya022.botcommands.internal.core.reflection.toAggregatorFunction
import io.github.freya022.botcommands.internal.options.CommandOptions
import io.github.freya022.botcommands.internal.options.transform
import io.github.freya022.botcommands.internal.parameters.AbstractMethodParameter
import io.github.freya022.botcommands.internal.parameters.AggregatedParameterMixin
import kotlin.reflect.KClass

internal class TimeoutHandlerParameter internal constructor(
    override val executable: TimeoutDescriptor<*>,
    aggregateBuilder: OptionAggregateBuilderImpl<*>,
    aggregatorFirstParamType: KClass<*>
) : AbstractMethodParameter(aggregateBuilder.parameter),
    AggregatedParameterMixin {

    override val aggregator = aggregateBuilder.aggregator.toAggregatorFunction(context, aggregatorFirstParamType)

    override val nestedAggregatedParameters = aggregateBuilder.optionAggregateBuilders.transform {
        TimeoutHandlerParameter(executable, it as OptionAggregateBuilderImpl<*>, aggregatorFirstParamType)
    }

    override val options = CommandOptions.transform(
        this,
        null,
        aggregateBuilder,
        optionFinalizer = ::TimeoutHandlerOption
    )
}