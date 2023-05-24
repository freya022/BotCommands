package com.freya02.botcommands.internal

import com.freya02.botcommands.internal.core.reflection.MemberEventFunction
import com.freya02.botcommands.internal.parameters.MethodParameter

interface IExecutableInteractionInfo {
    val function: MemberEventFunction<*, *>
    val instance: Any
        get() = function.instance
    val parameters: List<MethodParameter>
}