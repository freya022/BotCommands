package com.freya02.botcommands.internal.core

import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.getDeepestCause
import mu.KotlinLogging
import java.util.*

internal class ExceptionContextInfo {
    var logMessage: () -> String = { "An unhandled exception has been caught" }
    var dispatchMessage: () -> String = { "An exception has occurred" }
    var postRun: suspend () -> Unit = { }
}

internal class ExceptionContext private constructor(private val context: BContextImpl) {
    private val descStack: Stack<String> = Stack()

    inline fun <R> exceptionContext(desc: String, block: ExceptionContext.() -> R): R {
        descStack += desc
        return let(block).also { descStack.pop() }
    }

    suspend inline fun <R> overrideHandler(
        contextBlock: ExceptionContextInfo.() -> Unit,
        block: ExceptionContext.() -> R
    ): Result<R> = runCatching(block).onFailure { e ->
        val exceptionContextInfo = ExceptionContextInfo().apply(contextBlock)

        val baseEx = e.getDeepestCause()

        val descStr = descStack.joinToString("\n\t          ")
        val contextStr = "\tContext:  $descStr"

        KotlinLogging.logger {}.error(exceptionContextInfo.logMessage() + "\n$contextStr", baseEx)
        context.dispatchException(exceptionContextInfo.dispatchMessage(), baseEx)
        exceptionContextInfo.postRun()
    }

    companion object {
        suspend inline fun <R> exceptionContext(
            context: BContextImpl,
            desc: String,
            contextBlock: ExceptionContextInfo.() -> Unit,
            block: ExceptionContext.() -> R
        ): Result<R> {
            return ExceptionContext(context).overrideHandler(contextBlock) {
                exceptionContext(desc, block)
            }
        }
    }
}
