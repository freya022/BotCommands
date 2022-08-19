package com.freya02.botcommands.internal.components

import com.freya02.botcommands.api.parameters.ComponentParameterResolver
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.ExecutableInteractionInfo
import com.freya02.botcommands.internal.MethodParameters
import kotlin.reflect.KFunction

class ComponentDescriptor(
    context: BContextImpl,
    override val instance: Any,
    override val method: KFunction<*>
) : ExecutableInteractionInfo {
    override val parameters: MethodParameters

    init {
        @Suppress("RemoveExplicitTypeArguments") //Compiler bug
        parameters = MethodParameters.transform<ComponentParameterResolver<*, *>>(context, method) {
            optionTransformer = { parameter, _, resolver -> ComponentHandlerParameter(parameter, resolver) }
        }
    }
}