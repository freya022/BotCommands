package com.freya02.botcommands.internal.core

import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.utils.FunctionFilter
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

private typealias ClassPathFunctionIterable = Iterable<ClassPathFunction>

internal sealed interface ClassPathFunction {
    val instance: Any

    val function: KFunction<*>

    operator fun component1() = instance
    operator fun component2() = function
}

internal class LazyClassPathFunction internal constructor(
    private val context: BContextImpl,
    private val clazz: KClass<*>,
    override val function: KFunction<*>
) : ClassPathFunction {
    override val instance by lazy { context.serviceContainer.getService(clazz) }
}

internal fun ClassPathFunction(context: BContextImpl, clazz: KClass<*>, function: KFunction<*>): ClassPathFunction {
    return LazyClassPathFunction(context, clazz, function)
}

internal class InstanceClassPathFunction internal constructor(
    override val instance: Any,
    override val function: KFunction<*>
) : ClassPathFunction

internal fun Iterable<KFunction<*>>.toClassPathFunctions(instance: Any) = map { ClassPathFunction(instance, it) }

internal fun ClassPathFunction(instance: Any, function: KFunction<*>): ClassPathFunction {
    return InstanceClassPathFunction(instance, function)
}

internal fun <C : ClassPathFunctionIterable> C.withFilter(filter: FunctionFilter) = this.filter { filter(it.function, false) }
internal fun <C : ClassPathFunctionIterable> C.requiredFilter(filter: FunctionFilter) = this.onEach { filter(it.function, true) }