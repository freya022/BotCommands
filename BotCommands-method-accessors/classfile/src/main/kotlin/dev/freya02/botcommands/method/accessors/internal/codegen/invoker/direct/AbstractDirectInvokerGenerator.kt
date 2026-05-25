package dev.freya02.botcommands.method.accessors.internal.codegen.invoker.direct

import dev.freya02.botcommands.method.accessors.internal.codegen.AbstractClassFileMethodAccessorGenerator
import dev.freya02.botcommands.method.accessors.internal.codegen.ClassFileMemberMethodAccessorGenerator
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
        val javaParameterTypes = when (this) {
            // For inner class constructors, drop outer class argument as it is handled separately
            is ClassFileMemberMethodAccessorGenerator<*> if isInnerClassConstructor -> executable.parameterTypes.drop(1)
            else -> executable.parameterTypes.toList()
        }
        nonInstanceParameters.forEachIndexed { index, parameter ->
            val javaParameterType = javaParameterTypes[index]

            // <parameter> = (<type>) args.get([index])
            codeBuilder.loadArg(argsSlot, index)
            codeBuilder.unboxOrCastTo(javaParameterType, parameter.type.jvmErasure)
        }
    }
}
