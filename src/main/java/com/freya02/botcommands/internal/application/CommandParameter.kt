package com.freya02.botcommands.internal.application

import com.freya02.botcommands.api.application.builder.OptionBuilder
import com.freya02.botcommands.internal.findName
import com.freya02.botcommands.internal.parameters.MethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameterType
import com.freya02.botcommands.internal.throwUser
import kotlin.reflect.KParameter

abstract class CommandParameter(
    final override val kParameter: KParameter, optionBuilder: OptionBuilder
) : MethodParameter {
    override val methodParameterType = MethodParameterType.COMMAND

    override val name = optionBuilder.name

    init {
        val paramName = kParameter.findName()
        val optionName = optionBuilder.name
        if (paramName != optionName) {
            throwUser("Parameter '$kParameter' does not have the same name as the command declaration: '$optionName'")
        }
    }
}