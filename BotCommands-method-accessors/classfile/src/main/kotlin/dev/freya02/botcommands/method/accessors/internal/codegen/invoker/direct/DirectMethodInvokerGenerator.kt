package dev.freya02.botcommands.method.accessors.internal.codegen.invoker.direct

import dev.freya02.botcommands.method.accessors.internal.codegen.AbstractClassFileMethodAccessorGenerator
import java.lang.classfile.CodeBuilder
import java.lang.classfile.TypeKind
import java.lang.constant.MethodTypeDesc
import java.lang.reflect.Method

internal object DirectMethodInvokerGenerator : AbstractDirectInvokerGenerator() {

    override fun AbstractClassFileMethodAccessorGenerator<*>.generate(
        continuationSlot: Int?,
        codeBuilder: CodeBuilder,
    ) {
        require(executable is Method)

        val methodTypeDesc = run<MethodTypeDesc> {
            val returnTypeDesc = executable.returnType.describeConstable().get()
            val parameterDescs = executable.parameters.map { it.type.describeConstable().get() }
            MethodTypeDesc.of(returnTypeDesc, parameterDescs)
        }

        val thisSlot = codeBuilder.receiverSlot()
        val argsSlot = codeBuilder.parameterSlot(0)

        val parameterSlot = codeBuilder.allocateLocal(TypeKind.REFERENCE)

        // this.instance.[methodName]([params])
        if (!isStatic) {
            codeBuilder.aload(thisSlot)
            codeBuilder.getfield(thisClass, "instance", instanceDesc)
        }
        loadParameters(thisSlot, parameterSlot, argsSlot, codeBuilder)
        if (continuationSlot != null) codeBuilder.aload(continuationSlot)
        if (isStatic) {
            codeBuilder.invokestatic(instanceDesc, executable.name, methodTypeDesc)
        } else if (isInterface) {
            codeBuilder.invokeinterface(instanceDesc, executable.name, methodTypeDesc)
        } else {
            codeBuilder.invokevirtual(instanceDesc, executable.name, methodTypeDesc)
        }
    }
}
