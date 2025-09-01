package dev.freya02.botcommands.method.accessors.internal.exceptions

import dev.freya02.botcommands.method.accessors.internal.MethodAccessor

/**
 * Indicates a suspend function was called without calling [MethodAccessor.callSuspend].
 */
class IllegalSuspendCallException : RuntimeException("Suspending functions must be called with 'callSuspend'")
