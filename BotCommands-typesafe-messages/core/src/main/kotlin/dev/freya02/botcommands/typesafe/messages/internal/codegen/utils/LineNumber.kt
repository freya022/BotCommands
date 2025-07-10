package dev.freya02.botcommands.typesafe.messages.internal.codegen.utils

import java.lang.classfile.CodeBuilder

internal class LineNumber internal constructor(
    private val codeBuilder: CodeBuilder,
) {

    private var line = 1

    internal fun setAndIncrement() {
        codeBuilder.lineNumber(line++)
    }
}
