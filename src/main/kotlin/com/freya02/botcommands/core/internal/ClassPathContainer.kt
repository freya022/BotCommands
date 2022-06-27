package com.freya02.botcommands.core.internal

import com.freya02.botcommands.core.api.config.BConfig
import com.freya02.botcommands.internal.isStatic
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

internal class ClassPathFunction(val instance: Any, val function: KFunction<*>)

internal class ClassPathContainer(bConfig: BConfig, serviceContainer: ServiceContainer) {
    val classes: List<Any>
    val functions: List<ClassPathFunction>

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

        this.functions = classes
            .associate { serviceContainer.getService(it)!! to it.declaredMemberFunctions }
            .flatMap { entry -> entry.value.map { ClassPathFunction(entry.key, it) } }
    }

    inline fun <reified T : Annotation> functionsWithAnnotation() = functions.filter { it.function.hasAnnotation<T>() }
}

internal fun List<ClassPathFunction>.withReturnType(vararg types: KClass<*>) =
    this.map { it.function }.filter { it.returnType.jvmErasure.isSubclassOfAny(*types) }

internal fun <C : Iterable<ClassPathFunction>> C.requireReturnType(vararg types: KClass<*>): C = this.apply {
    for (func in this.map { it.function }) {
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

internal fun List<ClassPathFunction>.withFirstArg(vararg types: KClass<*>) = this.map { it.function }.filter { hasFirstArg(it, types) }

internal fun <C : Iterable<ClassPathFunction>> C.requireFirstArg(vararg types: KClass<*>): C = this.apply {
    for (func in this.map { it.function }) {
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

internal fun List<ClassPathFunction>.withNonStatic() = this.map { it.function }.filter { !it.isStatic }

internal fun <C : Iterable<ClassPathFunction>> C.requireNonStatic(): C = this.apply {
    for (func in this.map { it.function }) {
        requireUser(!func.isStatic, func) {
            "Function must be static"
        }
    }
}