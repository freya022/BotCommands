package dev.freya02.botcommands.method.accessors.internal.utils

import java.lang.reflect.Executable
import java.lang.reflect.Modifier
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaMethod

val KFunction<*>.javaExecutable: Executable
    get() = javaMethod ?: javaConstructor ?: error("Could not get executable of $this")

val Class<*>.isInnerClass: Boolean
    get() = isMemberClass && !Modifier.isStatic(modifiers)