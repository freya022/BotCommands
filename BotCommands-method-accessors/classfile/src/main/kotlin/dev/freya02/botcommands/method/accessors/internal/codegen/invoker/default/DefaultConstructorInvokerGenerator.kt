package dev.freya02.botcommands.method.accessors.internal.codegen.invoker.default

import dev.freya02.botcommands.method.accessors.internal.codegen.AbstractClassFileMethodAccessorGenerator
import dev.freya02.botcommands.method.accessors.internal.codegen.ClassFileMemberMethodAccessorGenerator
import dev.freya02.botcommands.method.accessors.internal.codegen.utils.CD_DefaultConstructorMarker
import java.lang.classfile.CodeBuilder
import java.lang.classfile.TypeKind
import java.lang.constant.ConstantDescs.*
import java.lang.constant.MethodTypeDesc
import java.lang.reflect.Constructor

internal object DefaultConstructorInvokerGenerator : AbstractDefaultInvokerGenerator() {

    override fun AbstractClassFileMethodAccessorGenerator<*>.generate(
        continuationSlot: Int?,
        codeBuilder: CodeBuilder,
    ) {
        require(executable is Constructor<*>)
        require(continuationSlot == null) { "Constructors cannot be suspending" }

        val methodTypeDesc = run {
            val parameterDescs = executable.parameters.map { it.type.describeConstable().get() }
            val effectiveParameters = parameterDescs + listOf(CD_int, CD_DefaultConstructorMarker)
            MethodTypeDesc.of(CD_void, effectiveParameters)
        }

        val thisSlot = codeBuilder.receiverSlot()
        val argsSlot = codeBuilder.parameterSlot(0)

        val maskSlot = codeBuilder.allocateLocal(TypeKind.INT)

        // maskSlot = 0
        codeBuilder.iconst_0()
        codeBuilder.istore(maskSlot)

        // new [className]
        codeBuilder.new_(instanceDesc)
        codeBuilder.dup() // So we can return it

        // <instance>."<init>"([params], mask, null)
        if (this is ClassFileMemberMethodAccessorGenerator<*> && isInnerClassConstructor) {
            // In an inner class, pass the outer instance as an argument
            codeBuilder.aload(thisSlot)
            codeBuilder.getfield(thisClass, "instance", effectiveInstanceDesc)
        }
        loadDefaultParameters(argsSlot, maskSlot, continuationSlot, codeBuilder)
        codeBuilder.invokespecial(instanceDesc, INIT_NAME, methodTypeDesc)
    }
}
