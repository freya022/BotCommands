package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.service.ClassGraphProcessor
import org.springframework.stereotype.Component

@Component
internal class SpringBotCommandsBootstrap internal constructor(
    config: BConfig,
    override val serviceContainer: SpringServiceContainer
) : AbstractBotCommandsBootstrap(config) {
    override val classGraphProcessors: Set<ClassGraphProcessor> = emptySet()

    init {
        init()
    }

    override suspend fun injectServices() {}
}