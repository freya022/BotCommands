package io.github.freya022.botcommands.internal.components.timeout

import io.github.freya022.botcommands.api.core.options.builder.OptionAggregateBuilder
import io.github.freya022.botcommands.api.parameters.resolvers.TimeoutParameterResolver
import io.github.freya022.botcommands.internal.CommandOptions
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.reflection.toAggregatorFunction
import io.github.freya022.botcommands.internal.parameters.IAggregatedParameter
import io.github.freya022.botcommands.internal.parameters.MethodParameterImpl
import io.github.freya022.botcommands.internal.transform
import kotlin.reflect.KClass

internal class TimeoutHandlerParameter internal constructor(
    context: BContextImpl,
    aggregateBuilder: OptionAggregateBuilder<*>,
    aggregatorFirstParamType: KClass<*>
) : MethodParameterImpl(aggregateBuilder.parameter), IAggregatedParameter {
    override val aggregator = aggregateBuilder.aggregator.toAggregatorFunction(context, aggregatorFirstParamType)

    override val nestedAggregatedParameters = aggregateBuilder.nestedAggregates.transform {
        TimeoutHandlerParameter(context, it, aggregatorFirstParamType)
    }

    override val options = CommandOptions.transform(
        context,
        null,
        aggregateBuilder,
        object : CommandOptions.Configuration<TimeoutHandlerOptionBuilder, TimeoutParameterResolver<*, *>> {
            override fun transformOption(
                optionBuilder: TimeoutHandlerOptionBuilder,
                resolver: TimeoutParameterResolver<*, *>
            ) = TimeoutHandlerOption(optionBuilder, resolver)
        })
}