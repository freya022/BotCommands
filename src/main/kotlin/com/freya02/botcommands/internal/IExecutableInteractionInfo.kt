package com.freya02.botcommands.internal

import com.freya02.botcommands.internal.parameters.MethodParameter
import kotlin.reflect.KFunction

interface IExecutableInteractionInfo {
    val instance: Any
    val method: KFunction<*>
    val parameters: List<MethodParameter>
    @Deprecated("There is no option parameters if everything is an aggregate")
    val optionParameters: List<MethodParameter>
}