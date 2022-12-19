package com.freya02.botcommands.api.commands.builder

import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.reflectReference
import kotlin.reflect.KFunction

@Suppress("UNCHECKED_CAST")
open class BuilderFunctionHolder<R> internal constructor() : IBuilderFunctionHolder<R> {
    override var function: KFunction<R> = NO_FUNCTION
        set(value) {
            field = value.reflectReference() as KFunction<R>
        }

    override fun isFunctionInitialized() = function !== NO_FUNCTION

    override fun checkFunction() {
        if (!isFunctionInitialized()) {
            throwUser("A command must have its function set")
        }
    }

    private object Dummy {
        fun dummy(): Nothing = throwInternal("A command with no function set has been used")
    }

    companion object {
        private val NO_FUNCTION = Dummy::dummy
    }
}