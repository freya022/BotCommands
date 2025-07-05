package io.github.freya022.botcommands.internal

import io.github.freya022.botcommands.api.core.Executable
import io.github.freya022.botcommands.internal.core.reflection.MemberParamFunction
import io.github.freya022.botcommands.internal.utils.requireAt
import io.github.freya022.botcommands.internal.utils.throwArgument
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.reflect.KFunction

internal interface ExecutableMixin : Executable {
    val eventFunction: MemberParamFunction<*, *>
    override val function: KFunction<*>
        get() = eventFunction.kFunction
    val instance: Any
        get() = eventFunction.instance
}

@Suppress("NOTHING_TO_INLINE") //Don't want this to appear in stack trace
internal inline fun ExecutableMixin.throwUser(message: String): Nothing = throwArgument(function, message)

@OptIn(ExperimentalContracts::class)
internal inline fun ExecutableMixin.requireUser(value: Boolean, lazyMessage: () -> String) {
    contract {
        returns() implies value
    }

    requireAt(value, function, lazyMessage)
}