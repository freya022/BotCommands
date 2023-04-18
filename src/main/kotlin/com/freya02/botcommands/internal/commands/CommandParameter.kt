package com.freya02.botcommands.internal.commands

import com.freya02.botcommands.api.commands.application.builder.OptionBuilder
import com.freya02.botcommands.internal.findDeclarationName
import com.freya02.botcommands.internal.parameters.MethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameterType
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionMetadata.isNullable
import kotlin.reflect.KParameter

abstract class CommandParameter(
    final override val kParameter: KParameter, optionBuilder: OptionBuilder
) : MethodParameter {
    override val methodParameterType = MethodParameterType.OPTION

    override val name = optionBuilder.declaredName
    val discordName = optionBuilder.optionName

    override val isOptional: Boolean by lazy { kParameter.isNullable || kParameter.isOptional }

    init {
        val paramName = kParameter.findDeclarationName()
        val declaredName = optionBuilder.declaredName
        if (paramName != declaredName) {
            throwUser("Parameter '$kParameter' does not have the same name as the command declaration: '$declaredName'")
        }
    }
}