package com.freya02.botcommands.internal.core

import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.getDeepestCause
import mu.KotlinLogging
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

    private suspend inline fun <R> runContext(descSupplier: Supplier<String>, block: ExceptionContext.() -> R): Result<R> {
        descStack += descSupplier
        return runCatching(block).onFailure { e ->
            val exceptionContextInfo = ExceptionContextInfo().apply(contextBlock)

            val baseEx = e.getDeepestCause()

            val descStr = StringJoiner("\n\t          ").apply { descStack.forEach { add(it.get()) } }.toString()
            val contextStr = "\tContext:  $descStr"

            //Compiler error when using the function type method
            KotlinLogging.logger(Logging.getLogger()).error(exceptionContextInfo.logMessage() + "\n$contextStr", baseEx)
            context.dispatchException(exceptionContextInfo.dispatchMessage(), baseEx)
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

        suspend inline fun <R> build(block: ExceptionContext.() -> R): Result<R> =
            ExceptionContext(context, contextBlock).runContext(descSupplier, block)
    }
}