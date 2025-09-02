package dev.freya02.botcommands.method.accessors.internal

import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

internal abstract class AbstractKotlinReflectMethodAccessor<R>(
    protected val function: KFunction<R>,
) : MethodAccessor<R> {

    protected val parameters = function.parameters.filter { it.kind != KParameter.Kind.INSTANCE }
    private val parameterCount = parameters.size

    override fun createBlankArguments(): MethodArguments {
        return MethodArguments(parameterCount)
    }

    protected inline fun argsToMap(args: MethodArguments, block: MutableMap<KParameter, Any?>.() -> Unit = {}): Map<KParameter, Any?> {
        return buildMap(args.size()) {
            block()
            parameters.forEachIndexed { index, parameter ->
                val arg = args[index]
                if (arg != MethodArguments.NO_VALUE) {
                    this[parameter] = arg
                }
            }
        }
    }
}
