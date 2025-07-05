package io.github.freya022.botcommands.internal.utils

import io.github.freya022.botcommands.api.core.annotations.IgnoreStackFrame
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import java.lang.StackWalker.StackFrame

@RequiresOptIn("Make sure internal classes are ignored", level = RequiresOptIn.Level.WARNING)
internal annotation class StackSensitive

internal val stackWalker: StackWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)

internal fun StackWalker.find(predicate: (StackFrame) -> Boolean): StackFrame? {
    return walk { stream -> stream.filter(predicate).findFirst() }.orElse(null)
}

internal fun <R : Any> StackWalker.firstNotNullOfOrNull(predicate: (StackFrame) -> R?): R? {
    return walk { stream -> stream.map { predicate(it) }.filter { it != null }.findFirst() }.orElse(null)
}

@PublishedApi
internal fun currentFrame(): StackFrame {
    return stackWalker.walk { stream ->
        stream
            // Filter out the synthetic method responsible for creating default parameters
            .filter { !it.methodName.endsWith("\$default") }
            .skip(1)
            .findFirst().get()
    }
}

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

val StackFrame.sourceFile: String
    get() = fileName
        ?: declaringClass.sourceFileOrNull
        ?: buildString {
            append("${declaringClass.canonicalName.split('.').first { it.any(Char::isUpperCase) }}.")
            if (declaringClass.isAnnotationPresent(Metadata::class.java)) {
                append("kt")
            } else {
                append("java")
            }
        }

@PublishedApi
internal fun StackFrame.toSignature() = "${declaringClass.simpleNestedName}.${methodName.substringBefore('$')} ($sourceFile:$lineNumber)"