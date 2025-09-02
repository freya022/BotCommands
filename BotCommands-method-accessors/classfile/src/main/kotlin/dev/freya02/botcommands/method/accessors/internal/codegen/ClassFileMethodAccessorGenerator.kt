package dev.freya02.botcommands.method.accessors.internal.codegen

import dev.freya02.botcommands.method.accessors.internal.MethodAccessor
import dev.freya02.botcommands.method.accessors.internal.utils.javaExecutable
import java.lang.invoke.MethodHandles
import java.lang.reflect.Modifier
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

internal object ClassFileMethodAccessorGenerator {

    internal fun <R> generate(
        instance: Any?,
        function: KFunction<R>,
        lookup: MethodHandles.Lookup,
    ): MethodAccessor<R> {
        function.parameters.forEach { parameter ->
            require(parameter.kind == KParameter.Kind.INSTANCE || parameter.kind == KParameter.Kind.VALUE) {
                "Unsupported parameter kind: $parameter"
            }
        }

        val generator = when {
            Modifier.isStatic(function.javaExecutable.modifiers) -> ClassFileStaticMethodAccessorGenerator(instance, function, lookup)
            else -> ClassFileMemberMethodAccessorGenerator(instance, function, lookup)
        }
        return generator.generate()
    }
}
