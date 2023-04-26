package com.freya02.botcommands.internal.commands

import com.freya02.botcommands.api.core.options.builder.OptionAggregateBuilder
import com.freya02.botcommands.internal.AbstractOption
import com.freya02.botcommands.internal.findDeclarationName
import com.freya02.botcommands.internal.parameters.MethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameterType
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionMetadata.isNullable
import kotlin.reflect.KParameter

abstract class CommandParameter(
    optionAggregateBuilder: OptionAggregateBuilder
) : MethodParameter {
    final override val kParameter: KParameter = optionAggregateBuilder.parameter

    final override val methodParameterType = MethodParameterType.OPTION

    final override val name = optionAggregateBuilder.declaredName
    //TODO rename to "options", not all options are command inputs
    abstract val commandOptions: List<AbstractOption>

    final override val isOptional: Boolean by lazy { kParameter.isNullable || kParameter.isOptional }

    init {
        val paramName = kParameter.findDeclarationName()
        val declaredName = optionAggregateBuilder.declaredName
        if (paramName != declaredName) {
            throwUser("Parameter '$kParameter' does not have the same name as the command declaration: '$declaredName'")
        }
    }
}