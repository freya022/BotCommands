package io.github.freya022.botcommands.api.core.service

import io.github.classgraph.ClassInfo
import io.github.classgraph.MethodInfo
import io.github.freya022.botcommands.api.core.BContext
import java.lang.reflect.Executable
import kotlin.reflect.KClass

interface ClassGraphProcessor {
    fun processClass(context: BContext, classInfo: ClassInfo, kClass: KClass<*>, isService: Boolean) {}

    fun processMethod(
        context: BContext,
        methodInfo: MethodInfo,
        method: Executable,
        classInfo: ClassInfo,
        kClass: KClass<*>,
        isServiceFactory: Boolean
    ) {}

    fun postProcess(context: BContext) {}
}