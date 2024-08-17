package io.github.freya022.botcommands.api.core.service

import io.github.classgraph.ClassInfo
import io.github.classgraph.MethodInfo
import java.lang.reflect.Executable
import kotlin.reflect.KClass

interface ClassGraphProcessor {
    fun processClass(classInfo: ClassInfo, kClass: KClass<*>, isService: Boolean) {}

    fun processMethod(
        methodInfo: MethodInfo,
        method: Executable,
        classInfo: ClassInfo,
        kClass: KClass<*>,
        isServiceFactory: Boolean,
    ) {}

    fun postProcess() {}
}