package com.freya02.botcommands.internal.core

import com.freya02.botcommands.internal.BContextImpl
import mu.KotlinLogging
import java.util.*

internal class ExceptionContextInfo {
    var logMessage: () -> String = { "An unhandled exception has been caught" }
    var dispatchMessage: () -> String = { "An exception has occurred" }
    var postRun: suspend () -> Unit = { }
}

//Kotlin either refuses to compile when some things are private
// Or doesn't notice source changes when too deeply inlined code is changed (i.e. runContext)
internal class ExceptionContext internal constructor(
    private val context: BContextImpl,
    private var contextBlock: ExceptionContextInfo.() -> Unit
) {
    internal constructor(currentContext: ExceptionContext, contextBlock: ExceptionContextInfo.() -> Unit) : this(currentContext.context, contextBlock) {
        descStack += currentContext.descStack
    }

    private var running = false
    private val descStack: Stack<() -> String> = Stack()

    inline fun <R> withExceptionContext(desc: String, block: ExceptionContext.() -> R) = withExceptionContext({ desc }, block)

    inline fun <R> withExceptionContext(noinline descSupplier: () -> String, block: ExceptionContext.() -> R): R {
        descStack += descSupplier
        return let(block).also { descStack.pop() }
    }

    fun withNewExceptionContext(contextBlock: ExceptionContextInfo.() -> Unit) = ExceptionContext(this, contextBlock)

    inline fun <R> overrideHandler(
        noinline contextBlock: ExceptionContextInfo.() -> Unit,
        block: ExceptionContext.() -> R
    ): R {
        val oldBlock = this.contextBlock
        this.contextBlock = contextBlock
        return let(block).also { this.contextBlock = oldBlock }
    }

    internal suspend inline fun <R> runContext(noinline descSupplier: () -> String, block: ExceptionContext.() -> R): Result<R> {
        if (running) throw IllegalStateException("This exception context is already active")
        running = true

        descStack += descSupplier
        return runCatching(block).onFailure { e ->
            val exceptionContextInfo = ExceptionContextInfo().apply(contextBlock)

            val descStr = descStack.joinToString("\n\t          ") { it() }
            val contextStr = "\tContext:  $descStr"

            KotlinLogging.logger { }.error(exceptionContextInfo.logMessage() + "\n$contextStr", e)

            context.dispatchException(exceptionContextInfo.dispatchMessage(), e)

            exceptionContextInfo.postRun()
        }.also {
            descStack.pop()
            running = false
        }
    }
}