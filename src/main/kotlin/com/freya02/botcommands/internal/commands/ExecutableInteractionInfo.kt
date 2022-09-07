package com.freya02.botcommands.internal.commands

import com.freya02.botcommands.api.builder.CommandBuilder
import com.freya02.botcommands.internal.*
import kotlin.reflect.KFunction
import kotlin.reflect.full.valueParameters

class ExecutableInteractionInfo(context: BContextImpl, builder: CommandBuilder) : IExecutableInteractionInfo {
    override val instance: Any
    override val method: KFunction<*>
    override val parameters: MethodParameters
        get() = throwInternal("Must be implemented by delegate host")

    init {
        instance = context.serviceContainer.getFunctionService(builder.function)
        method = builder.function

        requireUser(builder.optionBuilders.size == method.valueParameters.size - 1, method) {  //-1 for the event
            "Function must have the same number of options declared as on the method"
        }
    }
}