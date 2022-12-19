package com.freya02.botcommands.internal.commands

import com.freya02.botcommands.api.commands.builder.CommandBuilder
import com.freya02.botcommands.api.commands.builder.IBuilderFunctionHolder
import com.freya02.botcommands.api.commands.prefixed.builder.TextCommandVariationBuilder
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.parameters.MethodParameter
import kotlin.reflect.KFunction
import kotlin.reflect.full.valueParameters

class ExecutableInteractionInfo internal constructor(
    context: BContextImpl,
    builder: IBuilderFunctionHolder<*>
) : IExecutableInteractionInfo {
    override val instance: Any
    override val method: KFunction<*>
    override val parameters: MethodParameters
        get() = throwInternal("Must be implemented by delegate host")
    override val optionParameters: List<MethodParameter> by lazy { parameters.filterOptions() }

    init {
        builder as? IBuilderFunctionHolder<*> ?: throwMixin<IBuilderFunctionHolder<*>>()

        instance = context.serviceContainer.getFunctionService(builder.function)
        method = builder.function

        if (builder is CommandBuilder) {
            requireUser(builder.optionBuilders.size == method.valueParameters.size - 1, method) {  //-1 for the event
                "Function must have the same number of options declared as on the method"
            }
        } else if (builder is TextCommandVariationBuilder) {
            requireUser(builder.optionBuilders.size == method.valueParameters.size - 1, method) {  //-1 for the event
                "Function must have the same number of options declared as on the method"
            }
        }
    }

    companion object {
        internal inline fun <reified T : MethodParameter> List<MethodParameter>.filterOptions(): List<T> {
            return filter { it.isOption }.map {
                it as? T ?: throwMixin<T>()
            }
        }
    }
}