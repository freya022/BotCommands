package com.freya02.botcommands.internal.commands

import com.freya02.botcommands.api.commands.application.builder.OptionBuilder
import com.freya02.botcommands.internal.findDeclarationName
import com.freya02.botcommands.internal.parameters.MethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameterType
import com.freya02.botcommands.internal.throwUser
import kotlin.reflect.KParameter

abstract class CommandParameter(
    final override val kParameter: KParameter, optionBuilder: OptionBuilder
) : MethodParameter {
    override val methodParameterType = MethodParameterType.OPTION

    override val name = optionBuilder.declaredName
    override val discordName = optionBuilder.optionName

    init {
        val paramName = kParameter.findDeclarationName()
        val optionName = optionBuilder.declaredName
        if (paramName != optionName) {
            throwUser("Parameter '$kParameter' does not have the same name as the command declaration: '$optionName'")
        }
    }
}