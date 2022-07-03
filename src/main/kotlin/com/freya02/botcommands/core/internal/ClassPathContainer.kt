package com.freya02.botcommands.core.internal

import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.utils.ReflectionMetadata
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.nonInstanceParameters
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.jvmErasure
import kotlin.system.measureNanoTime

internal class InstanceDelegate(private val getter: () -> Any) {
    operator fun getValue(thisRef: Any, property: KProperty<*>): Any {
        return getter()
    }
}

internal class ClassPathFunction(instanceProvider: InstanceDelegate, val function: KFunction<*>) {
    val instance: Any by instanceProvider
}

internal class ClassPathContainer(private val context: BContextImpl) {
    val classes: List<KClass<*>>
    val functions: List<ClassPathFunction> by lazy {
        return@lazy retrieveClassFunctions()
    }

    init {
        val packages = context.config.packages
        val userClasses = context.config.classes

        val nano = measureNanoTime {
            val scanResult = ReflectionMetadata.runScan(packages, userClasses)

            ReflectionMetadata.readAnnotations(scanResult)

            this.classes = scanResult.loadClasses().map(Class<*>::kotlin)
        }

        println("Reflection took ${nano / 1000000.0} ms")
    }

    inline fun <reified T : Annotation> functionsWithAnnotation() = functions.filter { it.function.hasAnnotation<T>() }

    private fun retrieveClassFunctions(): List<ClassPathFunction> {
        return classes
            .associate { InstanceDelegate { context.serviceContainer.getService(it) } to it.declaredMemberFunctions }
            .flatMap { entry -> entry.value.map { ClassPathFunction(entry.key, it) } }
    }
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