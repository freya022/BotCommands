package com.freya02.botcommands.internal

import com.freya02.botcommands.internal.parameters.MethodParameter
import kotlin.reflect.KFunction

interface ExecutableInteractionInfo {
    val method: KFunction<*>
    val parameters: MethodParameters
    val optionParameters: List<MethodParameter>
        get() = parameters.filter { it.isOption }
    val instance: Any
}