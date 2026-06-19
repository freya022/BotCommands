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
        val clazz: Class<*>,
        val isService: Boolean,
    ) {
        // No need for a field here, the call is already cached
        val kClass: KClass<*> get() = clazz.kotlin
        val isProbablyObject: Boolean
            // Can't check field from ClassGraph object as it is private
            get() = classInfo.sourceFile?.endsWith("kt") == true && clazz.declaredFields.any { it.name == "INSTANCE" }
    }

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
