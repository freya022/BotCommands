package io.github.freya022.botcommands.method.accessors.internal

import kotlin.reflect.KParameter

interface MethodAccessor {

    // Return type is not specified due to some intricacies described in KCallable.callSuspendBy
    suspend fun call(args: Map<KParameter, Any?>): Any?
}
