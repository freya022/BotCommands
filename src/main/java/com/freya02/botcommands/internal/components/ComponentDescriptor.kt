package com.freya02.botcommands.internal.components

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.parameters.ComponentParameterResolver
import com.freya02.botcommands.internal.ConsumerEx
import com.freya02.botcommands.internal.ExecutableInteractionInfo
import com.freya02.botcommands.internal.MethodParameters
import com.freya02.botcommands.internal.runner.MethodRunner
import java.util.function.Consumer
import kotlin.reflect.KFunction

class ComponentDescriptor(
    context: BContext,
    override val instance: Any,
    override val method: KFunction<*>
) : ExecutableInteractionInfo {
    override val methodRunner: MethodRunner
    override val parameters: MethodParameters

    init {
        methodRunner = object : MethodRunner {
            //TODO replace
            @Suppress("UNCHECKED_CAST")
            override fun <R> invoke(
                args: Array<Any>,
                throwableConsumer: Consumer<Throwable>,
                successCallback: ConsumerEx<R>
            ) {
                try {
                    val call = method.call(*args)
                    successCallback.accept(call as R)
                } catch (e: Throwable) {
                    throwableConsumer.accept(e)
                }
            }
        }

        parameters = MethodParameters.of<ComponentParameterResolver>(method, mapOf()) { parameter, paramName, _ -> //TODO pass builders
            ComponentHandlerParameter(parameter, TODO()) //TODO take from DSL
        }
    }
}