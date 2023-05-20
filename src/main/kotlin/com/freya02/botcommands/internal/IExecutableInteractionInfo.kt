package com.freya02.botcommands.internal

import com.freya02.botcommands.internal.parameters.MethodParameter
import kotlin.reflect.KFunction

interface IExecutableInteractionInfo {
    val instance: Any
    val function: KFunction<*>
    val parameters: List<MethodParameter>
}