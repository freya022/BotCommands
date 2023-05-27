package com.freya02.botcommands.internal

import com.freya02.botcommands.internal.core.reflection.MemberEventFunction
import com.freya02.botcommands.internal.parameters.MethodParameter
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.reflect.KFunction

interface IExecutableInteractionInfo {
    val eventFunction: MemberEventFunction<*, *>
    val function: KFunction<*>
        get() = eventFunction.kFunction
    val instance: Any
        get() = eventFunction.instance
    val parameters: List<MethodParameter>
}

@Suppress("NOTHING_TO_INLINE") //Don't want this to appear in stack trace
internal inline fun IExecutableInteractionInfo.throwUser(message: String): Nothing = throwUser(function, message)

@OptIn(ExperimentalContracts::class)
internal inline fun IExecutableInteractionInfo.requireUser(value: Boolean, lazyMessage: () -> String) {
    contract {
        returns() implies value
    }

    requireUser(value, function, lazyMessage)
}