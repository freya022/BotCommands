package com.freya02.botcommands.internal

import com.freya02.botcommands.internal.parameters.MethodParameter
import com.freya02.botcommands.internal.runner.MethodRunner
import kotlin.reflect.KFunction

interface ExecutableInteractionInfo {
    val method: KFunction<*>
    val methodRunner: MethodRunner
    val parameters: MethodParameters
    val optionParameters: List<MethodParameter>
        get() = parameters.filter { it.isOption }
    val instance: Any
}