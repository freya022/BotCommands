package io.github.freya022.botcommands.method.accessors.internal

import kotlin.reflect.KFunction

interface MethodAccessorFactory {

    fun <R> create(instance: Any, function: KFunction<R>): MethodAccessor<R>
}
