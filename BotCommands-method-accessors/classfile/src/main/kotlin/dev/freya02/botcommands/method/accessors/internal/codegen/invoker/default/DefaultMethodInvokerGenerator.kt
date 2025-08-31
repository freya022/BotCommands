package dev.freya02.botcommands.method.accessors.internal.codegen.invoker.default

import dev.freya02.botcommands.method.accessors.internal.codegen.AbstractClassFileMethodAccessorGenerator
import java.lang.classfile.CodeBuilder
import java.lang.classfile.TypeKind
import java.lang.constant.ConstantDescs.CD_Object
import java.lang.constant.ConstantDescs.CD_int
import java.lang.constant.MethodTypeDesc
import java.lang.reflect.Method

internal object DefaultMethodInvokerGenerator : AbstractDefaultInvokerGenerator() {

    override fun AbstractClassFileMethodAccessorGenerator<*>.generate(
        continuationSlot: Int?,
        codeBuilder: CodeBuilder,
    ) {
        require(executable is Method)

        val methodTypeDesc = run<MethodTypeDesc> {
            val returnTypeDesc = executable.returnType.describeConstable().get()
            val parameterDescs = executable.parameters.map { it.type.describeConstable().get() }
            val effectiveParameters = when {
                isStatic -> parameterDescs + listOf(CD_int, CD_Object)
                else -> listOf(instanceDesc) + parameterDescs + listOf(CD_int, CD_Object)
            }
            MethodTypeDesc.of(returnTypeDesc, effectiveParameters)
        }

        val thisSlot = codeBuilder.receiverSlot()
        val argsSlot = codeBuilder.parameterSlot(0)

        val parameterSlot = codeBuilder.allocateLocal(TypeKind.REFERENCE)
        val maskSlot = codeBuilder.allocateLocal(TypeKind.INT)

        // maskSlot = 0
        codeBuilder.iconst_0()
        codeBuilder.istore(maskSlot)

        // InstanceClass.[methodName]$default(instance, [params], mask, null)
        if (!isStatic) {
            codeBuilder.aload(thisSlot)
            codeBuilder.getfield(thisClass, "instance", instanceDesc)
        }
        loadDefaultParameters(thisSlot, parameterSlot, argsSlot, maskSlot, continuationSlot, codeBuilder)
        codeBuilder.invokestatic(instanceDesc, $$"$${executable.name}$default", methodTypeDesc)
    }
}
