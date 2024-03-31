package io.github.freya022.botcommands.internal.utils

import io.github.freya022.botcommands.api.core.annotations.IgnoreStackFrame
import java.lang.StackWalker.StackFrame

internal val stackWalker: StackWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)

internal fun findCaller(skip: Long = 0): StackFrame {
    return stackWalker.walk { stream ->
        // Skip this method + the method calling this
        stream.skip(2 + skip)
            .filter { !it.declaringClass.isAnnotationPresent(IgnoreStackFrame::class.java) }
            .findFirst().get()
    }
}