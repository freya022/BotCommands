package com.freya02.botcommands.internal.core

import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.utils.FunctionFilter
import com.freya02.botcommands.internal.utils.ReflectionUtils.declaringClass
import mu.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

private typealias ClassPathFunctionIterable = Iterable<ClassPathFunction>

internal sealed interface ClassPathFunction {
    val instance: Any
    val instanceOrNull: Any?

    val function: KFunction<*>

    fun hasInstance() = instanceOrNull != null
}

internal inline fun <R> ClassPathFunction.withInstanceOrNull(block: (instance: Any, function: KFunction<*>) -> R) =
    instanceOrNull?.let { block(it, function) }

internal class LazyClassPathFunction internal constructor(
    private val context: BContextImpl,
    private val clazz: KClass<*>,
    override val function: KFunction<*>
) : ClassPathFunction {
    override val instance by lazy { context.serviceContainer.getService(clazz) }
    override val instanceOrNull: Any?
        get() = when (context.serviceContainer.canCreateService(clazz)) {
            null -> instance //No error message
            else -> null
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

@BService
@OptIn(ExperimentalTime::class)
internal class ClassPathContainer internal constructor(private val context: BContextImpl) {
    private val logger = KotlinLogging.logger { }

    //TODO filter out classes which aren't instantiable, remove the comment below and those related to it
    // Also remove ConditionalUse
    // To achieve this, this service should be created on post load (or add a pre ready status)
    private val instantiableClasses: List<KClass<*>> = context.serviceContainer.classes.filter { context.serviceContainer.canCreateService(it) == null }
    private val functions: List<ClassPathFunction>

    init {
        val (functions, duration) = measureTimedValue {
            retrieveClassFunctions()
        }
        this.functions = functions
        logger.trace { "Functions reflection took ${duration.toDouble(DurationUnit.MILLISECONDS)} ms" }
    }

    internal inline fun <reified A : Annotation> functionsWithAnnotation() = functions.withFilter(FunctionFilter.annotation<A>())

    private fun retrieveClassFunctions(): List<ClassPathFunction> {
        return instantiableClasses.flatMap { clazz ->
            clazz.declaredMemberFunctions
                .filter { it.annotations.isNotEmpty() } //Ignore methods without annotations, as this class only finds functions with annotations
                //TODO since all classes are guaranteed to be instantiable at this point, refactor CPF
                .map { ClassPathFunction(context, clazz, it) }
        }
    }

    companion object {
        fun Iterable<KFunction<*>>.toClassPathFunctions(instance: Any) = map { ClassPathFunction(instance, it) }
        fun Iterable<KFunction<*>>.toClassPathFunctions(context: BContextImpl) = map { ClassPathFunction(context, it.declaringClass, it) }
    }
}

internal fun <C : ClassPathFunctionIterable> C.withFilter(filter: FunctionFilter) = this.filter { filter(it.function, false) }
internal fun <C : ClassPathFunctionIterable> C.requiredFilter(filter: FunctionFilter) = this.onEach { filter(it.function, true) }