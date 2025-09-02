package dev.freya02.botcommands.method.accessors.internal.codegen.invoker.direct

import dev.freya02.botcommands.method.accessors.internal.codegen.AbstractClassFileMethodAccessorGenerator
import dev.freya02.botcommands.method.accessors.internal.codegen.invoker.AbstractInvokerGenerator
import java.lang.classfile.CodeBuilder
import java.lang.reflect.Constructor
import java.lang.reflect.Method

internal object DirectInvokerGenerator : AbstractInvokerGenerator() {

    override fun AbstractClassFileMethodAccessorGenerator<*>.generate(
        continuationSlot: Int?,
        codeBuilder: CodeBuilder,
    ) {
        val generator = when (executable) {
            is Method -> DirectMethodInvokerGenerator
            is Constructor<*> -> DirectConstructorInvokerGenerator
        }

        with(generator) { generate(continuationSlot, codeBuilder) }
    }
}
