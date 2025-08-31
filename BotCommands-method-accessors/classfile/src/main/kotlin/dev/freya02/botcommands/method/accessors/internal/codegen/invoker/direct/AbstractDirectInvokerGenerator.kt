package dev.freya02.botcommands.method.accessors.internal.codegen.invoker.direct

import dev.freya02.botcommands.method.accessors.internal.codegen.AbstractClassFileMethodAccessorGenerator
import dev.freya02.botcommands.method.accessors.internal.codegen.invoker.AbstractInvokerGenerator
import java.lang.classfile.CodeBuilder
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.jvmErasure

internal abstract class AbstractDirectInvokerGenerator : AbstractInvokerGenerator() {

    protected fun AbstractClassFileMethodAccessorGenerator<*>.loadParameters(
        thisSlot: Int,
        parameterSlot: Int,
        argsSlot: Int,
        codeBuilder: CodeBuilder,
    ) {
        function.parameters.forEachIndexed { index, parameter ->
            if (parameter.kind != KParameter.Kind.VALUE) return@forEachIndexed

            // var parameter = function.getParameters().get([index])
            loadParameter(codeBuilder, thisSlot, index, parameterSlot)

            // <parameter> = args.get(parameter)
            codeBuilder.loadArg(argsSlot, parameterSlot, parameter.type.jvmErasure.java)
        }
    }
}
