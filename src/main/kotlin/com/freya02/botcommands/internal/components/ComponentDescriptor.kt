package com.freya02.botcommands.internal.components

import com.freya02.botcommands.api.commands.builder.CustomOptionBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.IExecutableInteractionInfo
import com.freya02.botcommands.internal.parameters.OptionParameter
import com.freya02.botcommands.internal.transformParameters
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.jvmErasure

class ComponentDescriptor(
    context: BContextImpl,
    override val instance: Any,
    override val method: KFunction<*>
) : IExecutableInteractionInfo {
    override val parameters: List<ComponentHandlerParameter>

    init {
        parameters = method.nonInstanceParameters.drop(1).transformParameters(
            builderBlock = { function, parameter, declaredName ->
                val service = context.serviceContainer.peekServiceOrNull(parameter.type.jvmErasure)
                when {
                    service != null -> CustomOptionBuilder(OptionParameter.fromSelfAggregate(function, declaredName))
                    else -> ComponentHandlerOptionBuilder(OptionParameter.fromSelfAggregate(function, declaredName))
                }
            },
            aggregateBlock = { ComponentHandlerParameter(context, it) }
        )
    }
}