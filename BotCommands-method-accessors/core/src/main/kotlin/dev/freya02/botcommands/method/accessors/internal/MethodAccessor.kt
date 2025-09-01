package dev.freya02.botcommands.method.accessors.internal

import kotlin.reflect.KParameter

interface MethodAccessor<R> {

    suspend fun callSuspend(args: Map<KParameter, Any?>): R

    fun call(args: Map<KParameter, Any?>): R
}
