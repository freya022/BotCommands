package com.freya02.botcommands.internal.core

import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.utils.ReflectionMetadata
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import mu.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.jvmErasure
import kotlin.system.measureNanoTime

internal sealed interface ClassPathFunction {
    val instance: Any
    val instanceOrNull: Any?

    val function: KFunction<*>

    operator fun component1(): Any = instance
    operator fun component2(): KFunction<*> = function

    fun hasInstance() = instanceOrNull != null
}

internal class LazyClassPathFunction internal constructor(
    private val context: BContextImpl,
    private val clazz: KClass<*>,
    override val function: KFunction<*>
) : ClassPathFunction {
    override val instance
        get() = context.serviceContainer.getService(clazz)
    override val instanceOrNull: Any?
        get() {
            return when (context.serviceContainer.canCreateService(clazz)) {
                null -> instance //No error message
                else -> null
            }
        }
}

internal fun ClassPathFunction(context: BContextImpl, clazz: KClass<*>, function: KFunction<*>): ClassPathFunction {
    return LazyClassPathFunction(context, clazz, function)
}

internal class InstanceClassPathFunction internal constructor(
    override val instance: Any,
    override val function: KFunction<*>
) : ClassPathFunction {
    override val instanceOrNull: Any = instance
}

internal fun ClassPathFunction(instance: Any, function: KFunction<*>): ClassPathFunction {
    return InstanceClassPathFunction(instance, function)
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
            .flatMap { clazz ->
                clazz.declaredMemberFunctions.map { ClassPathFunction(context, clazz, it) }
            }
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