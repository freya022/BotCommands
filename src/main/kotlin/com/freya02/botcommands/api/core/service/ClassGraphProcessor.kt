package com.freya02.botcommands.api.core.service

import com.freya02.botcommands.api.core.BContext
import io.github.classgraph.ClassInfo
import io.github.classgraph.MethodInfo
import java.lang.reflect.Executable
import kotlin.reflect.KClass

interface ClassGraphProcessor {
    fun processClass(context: BContext, classInfo: ClassInfo, kClass: KClass<*>) {}

    fun processMethod(context: BContext, methodInfo: MethodInfo, method: Executable, classInfo: ClassInfo, kClass: KClass<*>) {}

    fun postProcess(context: BContext) {}
}