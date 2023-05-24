package com.freya02.botcommands.internal.core.reflection

import kotlin.reflect.KFunction

open class Function<R> internal constructor(val kFunction: KFunction<R>) : KFunction<R> by kFunction {
    override val parameters: List<Parameter> = kFunction.parameters.map { Parameter(it) }
}