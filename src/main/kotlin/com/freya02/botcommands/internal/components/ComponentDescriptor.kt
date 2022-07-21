package com.freya02.botcommands.internal.components

import com.freya02.botcommands.api.application.builder.CustomOptionBuilder
import com.freya02.botcommands.api.application.builder.OptionBuilder
import com.freya02.botcommands.api.parameters.ComponentParameterResolver
import com.freya02.botcommands.api.parameters.CustomResolver
import com.freya02.botcommands.commands.internal.ResolverContainer
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.ExecutableInteractionInfo
import com.freya02.botcommands.internal.MethodParameters
import com.freya02.botcommands.internal.findDeclarationName
import com.freya02.botcommands.internal.runner.MethodRunner
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.nonInstanceParameters
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
        val optionMap = method.nonInstanceParameters.drop(1).associate {
            val name = it.findDeclarationName()

            when (context.serviceContainer.getService(ResolverContainer::class).getResolver(it)) {
                is ComponentParameterResolver -> name to object : OptionBuilder(name) {}
                is CustomResolver -> name to CustomOptionBuilder(name)
                else -> TODO("Not implemented yet")
            }
        }

        parameters = MethodParameters.of<ComponentParameterResolver>(context, method, optionMap) { parameter, paramName, resolver ->
            ComponentHandlerParameter(parameter, optionMap[paramName]!!, resolver)
        }
    }
}