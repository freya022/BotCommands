package com.freya02.botcommands.api.builder

import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.reflectReference
import kotlin.reflect.KFunction

private object Dummy {
    fun dummy(): Nothing = throwInternal("A command with no function set has been used")
}

private val NO_FUNCTION = Dummy::dummy

@Suppress("UNCHECKED_CAST")
abstract class BuilderFunctionHolder<R> internal constructor() {
    open var function: KFunction<R> = NO_FUNCTION
        set(value) {
            field = value.reflectReference() as KFunction<R>
        }

    internal fun isFunctionInitialized() = function !== NO_FUNCTION
}