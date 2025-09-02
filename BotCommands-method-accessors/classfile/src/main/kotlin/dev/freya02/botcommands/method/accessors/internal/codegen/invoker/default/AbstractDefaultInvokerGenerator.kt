package dev.freya02.botcommands.method.accessors.internal.codegen.invoker.default

import dev.freya02.botcommands.method.accessors.internal.codegen.AbstractClassFileMethodAccessorGenerator
import dev.freya02.botcommands.method.accessors.internal.codegen.invoker.AbstractInvokerGenerator
import dev.freya02.botcommands.method.accessors.internal.codegen.utils.unboxOrCastTo
import java.lang.classfile.CodeBuilder
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.jvmErasure

internal abstract class AbstractDefaultInvokerGenerator : AbstractInvokerGenerator() {

    protected fun AbstractClassFileMethodAccessorGenerator<*>.loadDefaultParameters(
        argsSlot: Int,
        maskSlot: Int,
        continuationSlot: Int?,
        codeBuilder: CodeBuilder,
    ) {
        var valueParameterIndex = 0 // Used for mask calculation
        val nonInstanceParameters = function.parameters.filter { it.kind != KParameter.Kind.INSTANCE }
        val javaParameterTypes = executable.parameterTypes
        nonInstanceParameters.forEachIndexed { index, parameter ->
            val javaParameterType = javaParameterTypes[index]

            if (parameter.isOptional) {
                codeBuilder.loadUnboxedOptional(javaParameterType, parameter.type.jvmErasure, argsSlot, index, maskSlot, valueParameterIndex)
            } else {
                // <parameter> = args.get([index])
                codeBuilder.loadArg(argsSlot, index)
                codeBuilder.unboxOrCastTo(javaParameterType, parameter.type.jvmErasure)
            }

            valueParameterIndex++
        }
        if (continuationSlot != null) codeBuilder.aload(continuationSlot)
        codeBuilder.iload(maskSlot)
        codeBuilder.aconst_null()
    }
}
