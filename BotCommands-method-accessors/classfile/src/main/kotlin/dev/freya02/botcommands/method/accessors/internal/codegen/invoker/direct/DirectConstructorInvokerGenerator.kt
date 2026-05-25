package dev.freya02.botcommands.method.accessors.internal.codegen.invoker.direct

import dev.freya02.botcommands.method.accessors.internal.codegen.AbstractClassFileMethodAccessorGenerator
import dev.freya02.botcommands.method.accessors.internal.codegen.ClassFileMemberMethodAccessorGenerator
import java.lang.classfile.CodeBuilder
import java.lang.constant.ConstantDescs.CD_void
import java.lang.constant.ConstantDescs.INIT_NAME
import java.lang.constant.MethodTypeDesc
import java.lang.reflect.Constructor

internal object DirectConstructorInvokerGenerator : AbstractDirectInvokerGenerator() {

    override fun AbstractClassFileMethodAccessorGenerator<*>.generate(
        continuationSlot: Int?,
        codeBuilder: CodeBuilder,
    ) {
        require(executable is Constructor<*>)
        check(continuationSlot == null) { "Constructors cannot be suspending" }

        val methodTypeDesc = run {
            val parameterDescs = executable.parameters.map { it.type.describeConstable().get() }
            MethodTypeDesc.of(CD_void, parameterDescs)
        }

        val thisSlot = codeBuilder.receiverSlot()
        val argsSlot = codeBuilder.parameterSlot(0)

        // new [className]([params])
        codeBuilder.new_(instanceDesc)
        codeBuilder.dup() // So we can return it

        // <obj>.`<init>`([params])
        if (this is ClassFileMemberMethodAccessorGenerator<*> && isInnerClassConstructor) {
            // In an inner class, pass the outer instance as an argument
            codeBuilder.aload(thisSlot)
            codeBuilder.getfield(thisClass, "instance", effectiveInstanceDesc)
        }
        loadParameters(argsSlot, codeBuilder)
        codeBuilder.invokespecial(instanceDesc, INIT_NAME, methodTypeDesc)
    }
}
