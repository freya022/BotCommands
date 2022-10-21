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

//Kotlinc no happy when this private
internal class ExceptionContext @Deprecated("Do not use manually", replaceWith = ReplaceWith("ExceptionContext.build(context, contextBlock)")) internal constructor(
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
        return let(block).also { this.contextBlock = oldBlock }
    }

    //Kotlinc no happy when this private
    @Deprecated("Do not use manually")
    internal suspend inline fun <R> runContext(descSupplier: Supplier<String>, block: ExceptionContext.() -> R): Result<R> {
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

    companion object {
        @Suppress("DEPRECATION")
        suspend inline fun <R> create(
            context: BContextImpl,
            noinline contextBlock: ExceptionContextInfo.() -> Unit,
            descSupplier: Supplier<String>,
            block: ExceptionContext.() -> R
        ): Result<R> =
            ExceptionContext(context, contextBlock).runContext(descSupplier, block)
    }
}