package dev.freya02.botcommands.method.accessors.internal.codegen.invoker.direct

import dev.freya02.botcommands.method.accessors.internal.codegen.AbstractClassFileMethodAccessorGenerator
import dev.freya02.botcommands.method.accessors.internal.codegen.invoker.AbstractInvokerGenerator
import dev.freya02.botcommands.method.accessors.internal.codegen.utils.unboxOrCastTo
import java.lang.classfile.CodeBuilder
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.jvmErasure

internal abstract class AbstractDirectInvokerGenerator : AbstractInvokerGenerator() {

    protected fun AbstractClassFileMethodAccessorGenerator<*>.loadParameters(
        argsSlot: Int,
        codeBuilder: CodeBuilder,
    ) {
        val nonInstanceParameters = function.parameters.filter { it.kind != KParameter.Kind.INSTANCE }
        nonInstanceParameters.forEachIndexed { index, parameter ->
            // <parameter> = (<type>) args.get([index])
            codeBuilder.loadArg(argsSlot, index)
            codeBuilder.unboxOrCastTo(parameter.type.jvmErasure.java)
        }
    }
}
