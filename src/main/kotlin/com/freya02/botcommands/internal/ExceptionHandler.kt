package com.freya02.botcommands.internal

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.internal.utils.unwrap
import mu.KLogger
import net.dv8tion.jda.api.events.Event

internal class ExceptionHandler(private val context: BContext, private val logger: KLogger) {
    fun handleException(event: Event, e: Throwable, locationDescription: String) {
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