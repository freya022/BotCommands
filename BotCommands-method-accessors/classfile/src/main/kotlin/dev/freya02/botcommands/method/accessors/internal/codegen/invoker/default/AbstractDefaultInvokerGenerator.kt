package dev.freya02.botcommands.method.accessors.internal.codegen.invoker.default

import dev.freya02.botcommands.method.accessors.internal.codegen.AbstractClassFileMethodAccessorGenerator
import dev.freya02.botcommands.method.accessors.internal.codegen.invoker.AbstractInvokerGenerator
import java.lang.classfile.CodeBuilder
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.jvmErasure

internal abstract class AbstractDefaultInvokerGenerator : AbstractInvokerGenerator() {

    protected fun AbstractClassFileMethodAccessorGenerator<*>.loadDefaultParameters(
        thisSlot: Int,
        parameterSlot: Int,
        argsSlot: Int,
        maskSlot: Int,
        continuationSlot: Int?,
        codeBuilder: CodeBuilder,
    ) {
        var valueParameterIndex = 0
        function.parameters.forEachIndexed { index, parameter ->
            if (parameter.kind != KParameter.Kind.VALUE) return@forEachIndexed

            val paramJavaType = parameter.type.jvmErasure.java

            // var parameter = function.getParameters().get([index])
            loadParameter(codeBuilder, thisSlot, index, parameterSlot)

            if (parameter.isOptional) {
                codeBuilder.loadUnboxedOptional(paramJavaType, argsSlot, parameterSlot, maskSlot, valueParameterIndex)
            } else {
                // <parameter> = args.get(parameter)
                codeBuilder.loadArg(argsSlot, parameterSlot, paramJavaType)
            }

            valueParameterIndex++
        }
        if (continuationSlot != null) codeBuilder.aload(continuationSlot)
        codeBuilder.iload(maskSlot)
        codeBuilder.aconst_null()
    }
}
