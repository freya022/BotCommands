package com.freya02.botcommands.test_kt

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.GlobalExceptionHandlerAdapter
import com.freya02.botcommands.api.core.service.annotations.BService
import mu.KotlinLogging
import net.dv8tion.jda.api.events.Event

@BService
object MyGlobalExceptionHandler : GlobalExceptionHandlerAdapter() {
    private val logger = KotlinLogging.logger { }

    override fun handle(context: BContext, event: Event?, throwable: Throwable) {
        logger.error("Custom exception handling", throwable)
    }
}