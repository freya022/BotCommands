package dev.freya02.botcommands.jda.ktx.deprecation

import com.google.devtools.ksp.symbol.KSFile

data class CompatSourceFile(
    val processedFile: KSFile,
    val packageName: String,
    val fileName: String,
    val content: String,
)
