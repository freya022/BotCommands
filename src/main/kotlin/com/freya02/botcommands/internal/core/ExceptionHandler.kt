package com.freya02.botcommands.internal.core

import com.freya02.botcommands.api.core.EventDispatcher
import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.core.events.BExceptionEvent
import com.freya02.botcommands.internal.getDeepestCause
import mu.KotlinLogging

internal class ExceptionHandlerBuilder {
    var logMessage: () -> String = { "An unhandled exception has been caught" }
    var dispatchMessage: () -> String = { "An exception has occurred" }
    var postRun: suspend () -> Unit = { }
}

@BService
internal class ExceptionHandler(private val context: BContextImpl, private val eventDispatcher: EventDispatcher) {
    suspend inline fun <R> runCatching(event: Any, builderBlock: ExceptionHandlerBuilder.() -> Unit, block: () -> R) =
        runCatching(block).onFailure {
            handleException(it, event, builderBlock)
        }

    private suspend inline fun handleException(e: Throwable, event: Any, builderBlock: ExceptionHandlerBuilder.() -> Unit) {
        val builder = ExceptionHandlerBuilder().apply(builderBlock)

        eventDispatcher.dispatchEvent(BExceptionEvent(context, e, event))

        val baseEx = e.getDeepestCause()

        KotlinLogging.logger {}.error(builder.logMessage(), baseEx)
        context.dispatchException(builder.dispatchMessage(), baseEx)
        builder.postRun()
    }
}