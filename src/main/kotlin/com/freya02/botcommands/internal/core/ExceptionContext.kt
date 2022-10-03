package com.freya02.botcommands.internal.core

import com.freya02.botcommands.internal.BContextImpl
import mu.KotlinLogging
import org.slf4j.LoggerFactory
import java.util.*
import java.util.function.Supplier

internal class ExceptionContextInfo {
    var logMessage: () -> String = { "An unhandled exception has been caught" }
    var dispatchMessage: () -> String = { "An exception has occurred" }
    var postRun: suspend () -> Unit = { }
}

internal class ExceptionContext private constructor(
    private val context: BContextImpl,
    private var contextBlock: ExceptionContextInfo.() -> Unit
) {
    private val descStack: Stack<Supplier<String>> = Stack()

    inline fun <R> exceptionContext(desc: String, block: ExceptionContext.() -> R) = exceptionContext({ desc }, block)

    inline fun <R> exceptionContext(descSupplier: Supplier<String>, block: ExceptionContext.() -> R): R {
        descStack += descSupplier
        return let(block).also { descStack.pop() }
    }

    inline fun <R> overrideHandler(
        noinline contextBlock: ExceptionContextInfo.() -> Unit,
        block: ExceptionContext.() -> R
    ): R {
        val oldBlock = this.contextBlock
        this.contextBlock = contextBlock
        return block().also { this.contextBlock = oldBlock }
    }

    private suspend fun <R> runContext(descSupplier: Supplier<String>, block: ExceptionContext.() -> R): Result<R> {
        descStack += descSupplier
        return runCatching(block).onFailure { e ->
            val exceptionContextInfo = ExceptionContextInfo().apply(contextBlock)

            val descStr = descStack.joinToString("\n\t          ") { it.get() }
            val contextStr = "\tContext:  $descStr"

            KotlinLogging.logger(LoggerFactory.getLogger(e.stackTrace[0].className)).error(exceptionContextInfo.logMessage() + "\n$contextStr", e)

            context.dispatchException(exceptionContextInfo.dispatchMessage(), e)

            exceptionContextInfo.postRun()
        }.also { descStack.pop() }
    }

    internal class ExceptionContextBuilder(
        private val context: BContextImpl,
        private val descSupplier: Supplier<String>,
        private val contextBlock: ExceptionContextInfo.() -> Unit
    ) {
        constructor(context: BContextImpl, desc: String, contextBlock: ExceptionContextInfo.() -> Unit) : this(
            context,
            { desc },
            contextBlock
        )

        suspend inline fun <R> build(noinline block: ExceptionContext.() -> R): Result<R> =
            ExceptionContext(context, contextBlock).runContext(descSupplier, block)
    }
}