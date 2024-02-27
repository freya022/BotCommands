package io.github.freya022.botcommands.internal.core

import io.github.freya022.botcommands.api.core.service.lazy
import io.github.freya022.botcommands.internal.utils.FunctionFilter
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

private typealias ClassPathFunctionIterable = Iterable<ClassPathFunction>

internal sealed class ClassPathFunction {
    abstract val clazz: KClass<*>
    abstract val instance: Any

    abstract val function: KFunction<*>

    operator fun component1() = instance
    operator fun component2() = function

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ClassPathFunction) return false

        return function == other.function
    }

    override fun hashCode(): Int {
        return function.hashCode()
    }
}

internal class LazyClassPathFunction internal constructor(
    context: BContextImpl,
    override val clazz: KClass<*>,
    override val function: KFunction<*>
) : ClassPathFunction() {
    override val instance by context.serviceContainer.lazy(clazz)
}

internal fun ClassPathFunction(context: BContextImpl, clazz: KClass<*>, function: KFunction<*>): ClassPathFunction {
    return LazyClassPathFunction(context, clazz, function)
}

internal class InstanceClassPathFunction internal constructor(
    override val instance: Any,
    override val function: KFunction<*>
) : ClassPathFunction() {
    override val clazz: KClass<*> get() = instance::class
}

internal fun Iterable<KFunction<*>>.toClassPathFunctions(instance: Any) = map { ClassPathFunction(instance, it) }

internal fun ClassPathFunction(instance: Any, function: KFunction<*>): ClassPathFunction {
    return InstanceClassPathFunction(instance, function)
}

internal fun <C : ClassPathFunctionIterable> C.withFilter(filter: FunctionFilter) = this.filter { filter(it.function, false) }
internal fun <C : ClassPathFunctionIterable> C.requiredFilter(filter: FunctionFilter) = this.onEach { filter(it.function, true) }