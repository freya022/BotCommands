package com.freya02.botcommands.internal.core

import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.utils.ReflectionMetadata
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import mu.KotlinLogging
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

    constructor(obj: Any, function: KFunction<*>) : this(InstanceDelegate { obj }, function)
}

internal class ClassPathContainer(private val context: BContextImpl) {
    private val logger = KotlinLogging.logger { }

    val classes: List<KClass<*>>
    val functions: List<ClassPathFunction> by lazy {
        return@lazy retrieveClassFunctions()
    }

    init {
        val packages = context.config.packages
        val userClasses = context.config.classes

        if (packages.isEmpty()) {
            throwUser("You must specify at least 1 package to scan classes from")
        }

        val nano = measureNanoTime {
            this.classes = ReflectionMetadata.runScan(packages, userClasses).map(Class<*>::kotlin)
        }

        logger.trace { "Reflection took ${nano / 1000000.0} ms" }
    }

    inline fun <reified T : Annotation> functionsWithAnnotation() = functions.filter { it.function.hasAnnotation<T>() }

    private fun retrieveClassFunctions(): List<ClassPathFunction> {
        return classes
//            .filter { //Cannot predetermine availability of services when the framework is initializing as services may be injected and others might depend on those
//                val errorMessage = context.serviceContainer.canCreateService(it)
//                if (errorMessage != null) logger.trace { "Discarding ${it.simpleName} from ClassPathContainer, reason: $errorMessage" }
//                errorMessage == null
//            } //Keep services which can be loaded
            .associate { InstanceDelegate { context.getService(it) } to it.declaredMemberFunctions }
            .flatMap { entry -> entry.value.map { ClassPathFunction(entry.key, it) } }
    }

    companion object {
        inline fun <reified T : Annotation> Iterable<KFunction<*>>.filterWithAnnotation() = filter { it.hasAnnotation<T>() }

        fun Iterable<KFunction<*>>.toClassPathFunctions(instance: Any) = map { ClassPathFunction(instance, it) }
    }
}

internal fun List<ClassPathFunction>.withReturnType(vararg types: KClass<*>) =
    this.filter { it.function.returnType.jvmErasure.isSubclassOfAny(*types) }

internal fun <C : Iterable<ClassPathFunction>> C.requireReturnType(vararg types: KClass<*>): C = this.onEach {
    requireUser(it.function.returnType.jvmErasure.isSubclassOfAny(*types), it.function) {
        "Function must return any a superclass of: ${
            types.joinToString(
                prefix = "[",
                postfix = "]"
            ) { type -> type.java.simpleName }
        }"
    }
}

private fun hasFirstArg(
    it: KFunction<*>,
    types: Array<out KClass<*>>
) = when (val firstParam = it.nonInstanceParameters.firstOrNull()) {
    null -> false
    else -> firstParam.type.jvmErasure.isSubclassOfAny(*types)
}

internal fun List<ClassPathFunction>.withFirstArg(vararg types: KClass<*>) = this.filter { hasFirstArg(it.function, types) }

internal fun <C : Iterable<ClassPathFunction>> C.requireFirstArg(vararg types: KClass<*>): C = this.onEach {
    requireUser(hasFirstArg(it.function, types), it.function) {
        "Function must have a first parameter with a superclass of: ${
            types.joinToString(
                prefix = "[",
                postfix = "]"
            ) { type -> type.java.simpleName }
        }"
    }
}

internal fun List<ClassPathFunction>.withNonStatic() = this.filter { !it.function.isStatic }

internal fun <C : Iterable<ClassPathFunction>> C.requireNonStatic(): C = this.onEach {
    requireUser(!it.function.isStatic, it.function) {
        "Function must be static"
    }
}