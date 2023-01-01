package com.freya02.botcommands.internal.core

import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.utils.FunctionFilter
import com.freya02.botcommands.internal.utils.ReflectionMetadata
import mu.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.hasAnnotation
import kotlin.system.measureNanoTime

private typealias ClassPathFunctionIterable = Iterable<ClassPathFunction>

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

    inline fun <reified A : Annotation> functionsWithAnnotation() = functions.withFilter(FunctionFilter.annotation<A>())

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

internal fun <C : ClassPathFunctionIterable> C.withFilter(filter: FunctionFilter) = this.filter { filter(it.function, false) }
internal fun <C : ClassPathFunctionIterable> C.requiredFilter(filter: FunctionFilter) = this.onEach { filter(it.function, true) }