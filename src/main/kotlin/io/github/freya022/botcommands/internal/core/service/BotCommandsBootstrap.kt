package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.core.service.ClassGraphProcessor
import io.github.freya022.botcommands.api.core.service.ServiceContainer

internal interface BotCommandsBootstrap {
    val classGraphProcessors: Set<ClassGraphProcessor>
    val serviceContainer: ServiceContainer
}