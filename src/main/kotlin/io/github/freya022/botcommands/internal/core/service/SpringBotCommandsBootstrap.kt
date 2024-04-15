package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.config.BServiceConfig
import io.github.freya022.botcommands.api.core.service.ClassGraphProcessor
import io.github.freya022.botcommands.internal.utils.classRef
import io.github.freya022.botcommands.internal.utils.throwInternal
import org.springframework.stereotype.Component

@Component
internal class SpringBotCommandsBootstrap internal constructor(
    config: BConfig,
    serviceConfig: BServiceConfig,
    override val serviceContainer: SpringServiceContainer
) : AbstractBotCommandsBootstrap(config) {
    private var _stagingClassAnnotations: StagingClassAnnotations? = StagingClassAnnotations(serviceConfig)
    override val stagingClassAnnotations: StagingClassAnnotations
        get() = _stagingClassAnnotations
                ?: throwInternal("Cannot use ${classRef<StagingClassAnnotations>()} after it has been clearer")
    override val classGraphProcessors: Set<ClassGraphProcessor> = setOf(stagingClassAnnotations.processor)

    init {
        init()
    }

    override suspend fun injectServices() {}

    override fun clearStagingAnnotationsMap() {
        _stagingClassAnnotations = null
    }
}