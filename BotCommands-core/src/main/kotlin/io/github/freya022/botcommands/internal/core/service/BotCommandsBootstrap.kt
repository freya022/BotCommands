package io.github.freya022.botcommands.internal.core.service

import io.github.classgraph.ClassInfo
import io.github.classgraph.MethodInfo
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.internal.core.ClassPathProcessor

internal interface BotCommandsBootstrap {
    val classPathProcessors: Set<ClassPathProcessor>
    val serviceContainer: ServiceContainer

    fun isService(classInfo: ClassInfo): Boolean
    fun isServiceFactory(methodInfo: MethodInfo): Boolean
}
