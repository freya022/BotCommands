package dev.freya02.botcommands.method.accessors.internal.invoker.default

import dev.freya02.botcommands.method.accessors.internal.AbstractKotlinReflectMethodAccessor
import dev.freya02.botcommands.method.accessors.internal.MethodArguments
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

internal sealed class AbstractKotlinReflectDefaultMethodAccessor<R>(
    function: KFunction<R>,
) : AbstractKotlinReflectMethodAccessor<R>(function) {

    private val parameters = function.parameters.filter { it.kind != KParameter.Kind.INSTANCE }

    protected fun argsToMap(args: MethodArguments, instanceArg: Pair<KParameter, Any>?): Map<KParameter, Any?> {
        return buildMap(args.size()) {
            if (instanceArg != null) {
                this[instanceArg.first] = instanceArg.second
            }
            parameters.forEachIndexed { index, parameter ->
                val arg = args[index]
                if (arg != MethodArguments.NO_VALUE) {
                    this[parameter] = arg
                }
            }
        }
    }
}
