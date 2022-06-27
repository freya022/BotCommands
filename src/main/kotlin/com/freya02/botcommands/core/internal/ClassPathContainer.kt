package com.freya02.botcommands.core.internal

import com.freya02.botcommands.core.api.config.BConfig
import com.freya02.botcommands.internal.isSubclassOfAny
import com.freya02.botcommands.internal.requireUser
import com.freya02.botcommands.internal.utils.ReflectionMetadata
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.nonInstanceParameters
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.jvmErasure

internal class ClassPathContainer(bConfig: BConfig) {
    private val classes: List<Any>
    private val functions: List<KFunction<*>>

    init {
        val packages = bConfig.packages
        val userClasses = bConfig.classes

        val scanResult = ReflectionMetadata.runScan(packages, userClasses)

        ReflectionMetadata.readAnnotations(scanResult)

        this.classes = scanResult
            .allClasses
            .filter(ReflectionUtilsKt::isInstantiable)
            .loadClasses()
            .map(Class<*>::kotlin)

        this.functions = classes.flatMap(KClass<out Any>::declaredMemberFunctions)
    }

    inline fun <reified T : Annotation> functionsWithAnnotation() = functions.filter { it.hasAnnotation<T>() }
}

fun List<KFunction<*>>.withReturnType(vararg types: KClass<*>) =
    this.filter { it.returnType.jvmErasure.isSubclassOfAny(*types) }

fun <C : Iterable<KFunction<*>>> C.requireReturnType(vararg types: KClass<*>): C = this.apply {
    for (func in this) {
        requireUser(func.returnType.jvmErasure.isSubclassOfAny(*types), func) {
            "Function must return any a superclass of: ${
                types.joinToString(
                    prefix = "[",
                    postfix = "]"
                ) { it.java.simpleName }
            }"
        }
    }
}

private fun hasFirstArg(
    it: KFunction<*>,
    types: Array<out KClass<*>>
) = when (val firstParam = it.nonInstanceParameters.firstOrNull()) {
    null -> false
    else -> firstParam.type.jvmErasure.isSubclassOfAny(*types)
}

fun List<KFunction<*>>.withFirstArg(vararg types: KClass<*>) = this.filter { hasFirstArg(it, types) }

fun <C : Iterable<KFunction<*>>> C.requireFirstArg(vararg types: KClass<*>): C = this.apply {
    for (func in this) {
        requireUser(hasFirstArg(func, types), func) {
            "Function must have a first parameter with a superclass of: ${
                types.joinToString(
                    prefix = "[",
                    postfix = "]"
                ) { it.java.simpleName }
            }"
        }
    }
}