package com.freya02.botcommands.internal

import com.freya02.botcommands.internal.application.CommandParameter
import com.freya02.botcommands.internal.runner.MethodRunner
import kotlin.reflect.KFunction

interface ExecutableInteractionInfo {
    val method: KFunction<*>?
    val methodRunner: MethodRunner
    val parameters: MethodParameters<out CommandParameter<*>>
    val optionParameters: List<CommandParameter<*>>
        get() = parameters.filter { it.isOption }
    val instance: Any
}