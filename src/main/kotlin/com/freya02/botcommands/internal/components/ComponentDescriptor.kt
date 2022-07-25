package com.freya02.botcommands.internal.components

import com.freya02.botcommands.api.parameters.ComponentParameterResolver
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.ExecutableInteractionInfo
import com.freya02.botcommands.internal.MethodParameters
import com.freya02.botcommands.internal.MethodParameters2
import com.freya02.botcommands.internal.runner.MethodRunner
import kotlin.reflect.KFunction

class ComponentDescriptor(
    context: BContextImpl,
    override val instance: Any,
    override val method: KFunction<*>
) : ExecutableInteractionInfo {
    override val parameters: MethodParameters

    override val methodRunner: MethodRunner
        get() = TODO("To be removed")

    init {
        @Suppress("RemoveExplicitTypeArguments")
        parameters = MethodParameters2.transform<ComponentParameterResolver>(context, method) {
            optionTransformer = { parameter, _, resolver -> ComponentHandlerParameter(parameter, resolver) }
        }
    }
}