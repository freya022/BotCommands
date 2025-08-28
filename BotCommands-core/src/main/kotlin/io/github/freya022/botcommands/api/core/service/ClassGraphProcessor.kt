package io.github.freya022.botcommands.api.core.service

import io.github.classgraph.ClassInfo
import io.github.classgraph.MethodInfo
import io.github.freya022.botcommands.internal.core.annotations.SkipJavaReflectionOverload
import java.lang.reflect.Executable
import kotlin.reflect.KClass

interface ClassGraphProcessor {
    @SkipJavaReflectionOverload
    fun processClass(serviceContainer: ServiceContainer, classInfo: ClassInfo, kClass: KClass<*>, isService: Boolean) {}

    @SkipJavaReflectionOverload
    fun processMethod(
        serviceContainer: ServiceContainer,
        methodInfo: MethodInfo,
        method: Executable,
        classInfo: ClassInfo,
        kClass: KClass<*>,
        isServiceFactory: Boolean,
    ) {}

    fun postProcess(serviceContainer: ServiceContainer) {}
}
