package com.freya02.botcommands.internal.components

import com.freya02.botcommands.api.parameters.ParameterWrapper.Companion.wrap
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.IExecutableInteractionInfo
import com.freya02.botcommands.internal.transformParameters
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import kotlin.reflect.KFunction

class ComponentDescriptor(
    context: BContextImpl,
    override val instance: Any,
    override val method: KFunction<*>
) : IExecutableInteractionInfo {
    override val parameters: List<ComponentHandlerParameter>

    init {
        parameters = method.nonInstanceParameters.drop(1).transformParameters(
            builderBlock = { function, parameter, declaredName ->
                val service = context.serviceContainer.peekServiceOrNull(parameter.wrap().toVarargElementType().erasure)
                when {
//                    service != null -> CustomOptionBuilder(function, declaredName)
//                    else -> ComponentHandlerOptionBuildder(function, declaredName)
                    service != null -> TODO()
                    else -> TODO()
                }
            },
            aggregateBlock = { ComponentHandlerParameter(context, it) }
        )
    }
}