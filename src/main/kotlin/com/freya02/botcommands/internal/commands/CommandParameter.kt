package com.freya02.botcommands.internal.commands

import com.freya02.botcommands.api.core.options.builder.OptionAggregateBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.core.options.Option
import com.freya02.botcommands.internal.findDeclarationName
import com.freya02.botcommands.internal.parameters.MethodParameter
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionMetadata.isNullable
import kotlin.reflect.KParameter

abstract class CommandParameter(
    context: BContextImpl,
    optionAggregateBuilder: OptionAggregateBuilder
) : MethodParameter {
    final override val kParameter: KParameter = optionAggregateBuilder.parameter
    val aggregator = optionAggregateBuilder.aggregator
    val aggregatorInstance = context.serviceContainer.getFunctionService(optionAggregateBuilder.aggregator)

    final override val name = optionAggregateBuilder.declaredName
    //TODO rename to "options", not all options are command inputs
    abstract val commandOptions: List<Option>

    final override val isOptional: Boolean by lazy { kParameter.isNullable || kParameter.isOptional }

    init {
        val paramName = kParameter.findDeclarationName()
        val declaredName = optionAggregateBuilder.declaredName
        if (paramName != declaredName) {
            throwUser("Parameter '$kParameter' does not have the same name as the command declaration: '$declaredName'")
        }
    }
}