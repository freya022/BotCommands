package io.github.freya022.botcommands.internal.utils

import io.github.freya022.botcommands.api.core.annotations.IgnoreStackFrame
import java.lang.StackWalker.StackFrame

@RequiresOptIn("Make sure internal classes are ignored", level = RequiresOptIn.Level.WARNING)
annotation class StackSensitive

internal val stackWalker: StackWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)

/**
 * - 0 to find the direct caller
 * - 1 to find the caller of the caller
 */
@StackSensitive
internal fun findCaller(skip: Long = 0): StackFrame {
    return stackWalker.walk { stream ->
        stream
            // Filter out the synthetic method responsible for creating default parameters
            .filter { !it.methodName.endsWith("\$default") }
            // Skip this method + the method calling this
            .skip(2 + skip)
            .filter { !it.declaringClass.isAnnotationPresent(IgnoreStackFrame::class.java) }
            .findFirst().get()
    }
}