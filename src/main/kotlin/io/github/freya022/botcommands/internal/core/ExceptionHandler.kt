package io.github.freya022.botcommands.internal.core

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.utils.unwrap
import mu.KLogger
import net.dv8tion.jda.api.events.Event

internal class ExceptionHandler(private val context: BContext, private val logger: KLogger) {
    fun handleException(event: Event?, e: Throwable, locationDescription: String) {
        val unreflectedException = e.unwrap()
        val handler = context.globalExceptionHandler
        if (handler != null) {
            handler.onException(context, event, unreflectedException)
            return
        }

        val errorMessage = "Uncaught exception in $locationDescription"
        logger.error(errorMessage, unreflectedException)
        context.dispatchException(errorMessage, unreflectedException)
    }
}