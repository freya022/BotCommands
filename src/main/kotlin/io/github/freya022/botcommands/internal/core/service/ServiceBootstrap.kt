package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.core.service.ClassGraphProcessor
import io.github.freya022.botcommands.api.core.service.ServiceContainer


internal interface ServiceBootstrap {
    val classGraphProcessors: Set<ClassGraphProcessor>
    val stagingClassAnnotations: StagingClassAnnotations
    val serviceContainer: ServiceContainer

    fun clearStagingAnnotationsMap()
}