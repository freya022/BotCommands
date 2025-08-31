package dev.freya02.botcommands.method.accessors.internal.codegen.invoker

import dev.freya02.botcommands.method.accessors.internal.codegen.AbstractClassFileMethodAccessorGenerator
import java.lang.classfile.CodeBuilder

internal interface InvokerGenerator {

    fun AbstractClassFileMethodAccessorGenerator<*>.generate(continuationSlot: Int?, codeBuilder: CodeBuilder)
}
