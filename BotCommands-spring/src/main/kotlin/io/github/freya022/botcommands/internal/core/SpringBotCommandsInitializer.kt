package io.github.freya022.botcommands.internal.core

import io.github.freya022.botcommands.internal.core.service.SpringBotCommandsBootstrap
import org.springframework.beans.factory.getBean
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
internal data object SpringBotCommandsInitializer {
    @EventListener
    internal fun onAppReadyEvent(event: ApplicationReadyEvent) {
        val bootstrap = event.applicationContext.getBean<SpringBotCommandsBootstrap>()
        bootstrap.loadContext()
    }
}