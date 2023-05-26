package com.freya02.botcommands.internal

import com.freya02.botcommands.internal.core.reflection.MemberEventFunction
import com.freya02.botcommands.internal.core.reflection.throwUser
import com.freya02.botcommands.internal.parameters.MethodParameter
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

interface IExecutableInteractionInfo {
    val function: MemberEventFunction<*, *>
    val instance: Any
        get() = function.instance
    val parameters: List<MethodParameter>
}

@Suppress("NOTHING_TO_INLINE") //Don't want this to appear in stack trace
internal inline fun IExecutableInteractionInfo.throwUser(message: String): Nothing = function.throwUser(message)

@OptIn(ExperimentalContracts::class)
internal inline fun IExecutableInteractionInfo.requireUser(value: Boolean, lazyMessage: () -> String) {
    contract {
        returns() implies value
    }

    requireUser(value, function.kFunction, lazyMessage)
}