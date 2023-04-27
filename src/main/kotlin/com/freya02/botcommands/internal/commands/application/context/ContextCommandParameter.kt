package com.freya02.botcommands.internal.commands.application.context

import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandOptionAggregateBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.parameters.MethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameterType
import com.freya02.botcommands.internal.utils.ReflectionMetadata.isNullable

abstract class ContextCommandParameter(context: BContextImpl, optionAggregateBuilder: ApplicationCommandOptionAggregateBuilder) : MethodParameter {
    final override val methodParameterType = MethodParameterType.OPTION
    final override val kParameter = optionAggregateBuilder.parameter
    final override val isOptional: Boolean by lazy { kParameter.isNullable || kParameter.isOptional }

    val aggregator = optionAggregateBuilder.aggregator
    val aggregatorInstance = context.serviceContainer.getFunctionService(aggregator)
}