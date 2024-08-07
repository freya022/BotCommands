package io.github.freya022.botcommands.internal.core

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.utils.unwrap
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.Level
import net.dv8tion.jda.api.events.Event

internal class ExceptionHandler(private val context: BContext, private val logger: KLogger) {
    fun handleException(event: Event?, e: Throwable, locationDescription: String, extraContext: Map<String, Any?> = emptyMap(), level: Level = Level.ERROR) {
        val unreflectedException = e.unwrap()
        val handler = context.globalExceptionHandler
        if (handler != null) return handler.onException(event, unreflectedException)

        val errorMessage = "Uncaught exception in $locationDescription"
        logger.at(level) {
            cause = unreflectedException
            message = errorMessage
        }
        context.dispatchException(errorMessage, unreflectedException, extraContext)
    }
}