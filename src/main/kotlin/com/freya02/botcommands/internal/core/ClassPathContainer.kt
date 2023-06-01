package com.freya02.botcommands.internal.core

import com.freya02.botcommands.api.core.service.annotations.InjectedService
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.utils.FunctionFilter
import com.freya02.botcommands.internal.utils.ReflectionMetadata
import mu.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.system.measureNanoTime
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

@InjectedService
@OptIn(ExperimentalTime::class)
internal class ClassPathContainer(private val context: BContextImpl) {
    private val logger = KotlinLogging.logger { }

    val classes: List<KClass<*>>
    val functions: List<ClassPathFunction> by lazy {
        val (functions, duration) = measureTimedValue {
            retrieveClassFunctions()
        }
        logger.trace { "Functions reflection took ${duration.toDouble(DurationUnit.MILLISECONDS)} ms" }
        return@lazy functions
    }

    init {
        val nano = measureNanoTime {
            this.classes = ReflectionMetadata.runScan(context)
        }

        logger.trace { "Classes reflection took ${nano / 1000000.0} ms" }
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
        fun Iterable<KFunction<*>>.toClassPathFunctions(instance: Any) = map { ClassPathFunction(instance, it) }
    }
}

internal fun <C : ClassPathFunctionIterable> C.withFilter(filter: FunctionFilter) = this.filter { filter(it.function, false) }
internal fun <C : ClassPathFunctionIterable> C.requiredFilter(filter: FunctionFilter) = this.onEach { filter(it.function, true) }