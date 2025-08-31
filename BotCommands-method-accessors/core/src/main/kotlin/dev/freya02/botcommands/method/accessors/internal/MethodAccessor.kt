package dev.freya02.botcommands.method.accessors.internal

import kotlin.reflect.KParameter

interface MethodAccessor<R> {

    suspend fun call(args: Map<KParameter, Any?>): R
}
