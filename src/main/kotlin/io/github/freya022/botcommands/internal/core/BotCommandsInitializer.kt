package io.github.freya022.botcommands.internal.core

import io.github.freya022.botcommands.internal.core.service.SpringBotCommandsBootstrap
import org.springframework.stereotype.Component

@Component
internal class BotCommandsInitializer(serviceBootstrap: SpringBotCommandsBootstrap) {
    init {
        serviceBootstrap.runBootstrap()
    }
}