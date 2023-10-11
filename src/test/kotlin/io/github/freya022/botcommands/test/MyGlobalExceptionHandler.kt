package io.github.freya022.botcommands.test

import io.github.freya022.botcommands.api.core.GlobalExceptionHandlerAdapter
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.Event

@BService
object MyGlobalExceptionHandler : GlobalExceptionHandlerAdapter() {
    private val logger = KotlinLogging.logger { }

    override fun handle(event: Event?, throwable: Throwable) {
        logger.error(throwable) { "Custom exception handling" }
    }
}