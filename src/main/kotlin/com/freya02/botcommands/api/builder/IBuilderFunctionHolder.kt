package com.freya02.botcommands.api.builder

import kotlin.reflect.KFunction

internal interface IBuilderFunctionHolder<R> {
    var function: KFunction<R>

    fun isFunctionInitialized(): Boolean
    fun checkFunction()
}