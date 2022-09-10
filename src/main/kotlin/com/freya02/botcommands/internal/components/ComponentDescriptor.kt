package com.freya02.botcommands.internal.components

import com.freya02.botcommands.api.parameters.ComponentParameterResolver
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.IExecutableInteractionInfo
import com.freya02.botcommands.internal.MethodParameters
import com.freya02.botcommands.internal.commands.ExecutableInteractionInfo.Companion.filterOptions
import kotlin.reflect.KFunction

class ComponentDescriptor(
    context: BContextImpl,
    override val instance: Any,
    override val method: KFunction<*>
) : IExecutableInteractionInfo {
    override val parameters: MethodParameters
    override val optionParameters: List<ComponentHandlerParameter>

    init {
        @Suppress("RemoveExplicitTypeArguments") //Compiler bug
        parameters = MethodParameters.transform<ComponentParameterResolver<*, *>>(context, method) {
            optionTransformer = { parameter, _, resolver -> ComponentHandlerParameter(parameter, resolver) }
        }

        optionParameters = parameters.filterOptions()
    }
}