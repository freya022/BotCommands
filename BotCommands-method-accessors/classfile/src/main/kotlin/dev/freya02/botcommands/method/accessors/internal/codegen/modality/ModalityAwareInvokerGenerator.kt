package dev.freya02.botcommands.method.accessors.internal.codegen.modality

import dev.freya02.botcommands.method.accessors.internal.codegen.AbstractClassFileMethodAccessorGenerator
import dev.freya02.botcommands.method.accessors.internal.codegen.invoker.InvokerGenerator
import java.lang.classfile.CodeBuilder

internal interface ModalityAwareInvokerGenerator {

    fun AbstractClassFileMethodAccessorGenerator<*>.generate(
        invokerGenerator: InvokerGenerator,
        codeBuilder: CodeBuilder,
    )
}
