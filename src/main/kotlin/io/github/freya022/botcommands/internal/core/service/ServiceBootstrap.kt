package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.core.service.ClassGraphProcessor
import io.github.freya022.botcommands.api.core.service.ServiceContainer

//TODO rename to BotCommandsBootstrapper
// make this an abstract class, move default init logic in it
// so both default and spring initialize there instead of separate classes
internal interface ServiceBootstrap {
    val classGraphProcessors: Set<ClassGraphProcessor>
    val stagingClassAnnotations: StagingClassAnnotations
    val serviceContainer: ServiceContainer

    fun clearStagingAnnotationsMap()
}