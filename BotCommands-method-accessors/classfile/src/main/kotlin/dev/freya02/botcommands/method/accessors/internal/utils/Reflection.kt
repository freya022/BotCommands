package dev.freya02.botcommands.method.accessors.internal.utils

import java.lang.reflect.Executable
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaMethod

internal val KFunction<*>.javaExecutable: Executable
    get() = javaMethod ?: javaConstructor ?: error("Could not get executable of $this")
