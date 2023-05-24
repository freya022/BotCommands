package com.freya02.botcommands.internal.commands

import com.freya02.botcommands.api.commands.builder.ExecutableCommandBuilder
import com.freya02.botcommands.api.commands.builder.IBuilderFunctionHolder
import com.freya02.botcommands.api.commands.prefixed.builder.TextCommandVariationBuilder
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.parameters.MethodParameter
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonEventParameters
import kotlin.reflect.KFunction

class ExecutableInteractionInfo internal constructor(
    context: BContextImpl,
    builder: IBuilderFunctionHolder<*>
) : IExecutableInteractionInfo {
    override val instance: Any
    override val function: KFunction<*>
    override val parameters: List<MethodParameter>
        get() = throwInternal("Must be implemented by delegate host")

    init {
        builder as? IBuilderFunctionHolder<*> ?: throwMixin<IBuilderFunctionHolder<*>>()

        instance = context.serviceContainer.getFunctionService(builder.function)
        function = builder.function

        if (builder is ExecutableCommandBuilder<*, *>) {
            requireUser(builder.optionAggregateBuilders.size == function.nonEventParameters.size, function) {
                "Function must have the same number of options declared as on the method"
            }
        } else if (builder is TextCommandVariationBuilder) {
            requireUser(builder.optionAggregateBuilders.size == function.nonEventParameters.size, function) {
                "Function must have the same number of options declared as on the method"
            }
        }
    }
}