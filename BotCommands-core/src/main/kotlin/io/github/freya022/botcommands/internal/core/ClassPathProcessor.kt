package io.github.freya022.botcommands.internal.core

import io.github.classgraph.ClassInfo
import io.github.classgraph.MethodInfo
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.internal.core.annotations.SkipJavaReflectionOverload
import java.lang.reflect.Executable
import kotlin.reflect.KClass

interface ClassPathProcessor {
    @SkipJavaReflectionOverload
    fun processClass(data: ClassData) {}

    @SkipJavaReflectionOverload
    fun processMethod(data: MethodData) {}

    fun postProcess(data: PostProcessData) {}

    class ClassData internal constructor(
        val serviceContainer: ServiceContainer,
        val classInfo: ClassInfo,
        val kClass: KClass<*>,
        val isService: Boolean,
    )

    class MethodData internal constructor(
        val classData: ClassData,
        val methodInfo: MethodInfo,
        val method: Executable,
        val isServiceFactory: Boolean,
    ) {
        val serviceContainer: ServiceContainer get() = classData.serviceContainer
    }

    class PostProcessData internal constructor(val serviceContainer: ServiceContainer)
}
