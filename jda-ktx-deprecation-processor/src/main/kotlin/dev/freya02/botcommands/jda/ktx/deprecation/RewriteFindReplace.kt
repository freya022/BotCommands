package dev.freya02.botcommands.jda.ktx.deprecation

import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile

data class RewriteFindReplace(
    val processedFile: KSFile,
    val old: String,
    val new: String,
) {

    companion object {
        fun from(
            declaration: KSDeclaration,
            old: String,
            new: String,
        ) = RewriteFindReplace(declaration.containingFile!!, old, new)
    }
}
