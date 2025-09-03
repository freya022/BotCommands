package dev.freya02.botcommands.method.accessors.internal.codegen.modality

import dev.freya02.botcommands.method.accessors.internal.codegen.AbstractClassFileMethodAccessorGenerator
import dev.freya02.botcommands.method.accessors.internal.codegen.invoker.InvokerGenerator
import dev.freya02.botcommands.method.accessors.internal.codegen.utils.CD_Unit
import dev.freya02.botcommands.method.accessors.internal.codegen.utils.boxIfPrimitive
import java.lang.classfile.CodeBuilder
import java.lang.constant.MethodTypeDesc
import java.lang.reflect.Method
import kotlin.reflect.jvm.jvmErasure

internal object BlockingInvokerGenerator : ModalityAwareInvokerGenerator {

    override fun AbstractClassFileMethodAccessorGenerator<*>.generate(
        invokerGenerator: InvokerGenerator,
        codeBuilder: CodeBuilder,
    ) {
        with(invokerGenerator) { generate(continuationSlot = null, codeBuilder) }

        if (executable is Method) {
            // Return value as Object, or the value class box, or return Unit as the implemented method must return something
            val returnErasure = function.returnType.jvmErasure
            if (returnErasure.isValue) {
                val valueClassDesc = returnErasure.java.describeConstable().get()
                codeBuilder.invokestatic(valueClassDesc, "box-impl", MethodTypeDesc.of(valueClassDesc, executable.returnType.describeConstable().get()))
            } else if (executable.returnType != Void.TYPE) {
                codeBuilder.boxIfPrimitive(type = executable.returnType)
            } else {
                codeBuilder.getstatic(CD_Unit, "INSTANCE", CD_Unit)
            }
        }
        codeBuilder.areturn()
    }
}
