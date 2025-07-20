package dev.freya02.botcommands.jda.ktx.deprecation.ktx.processor

import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dev.freya02.botcommands.jda.ktx.deprecation.RewriteFindReplace
import dev.freya02.botcommands.jda.ktx.deprecation.utils.RichKotlinClassMetadata
import kotlin.metadata.jvm.KotlinClassMetadata

object JdaKtxFunctionProcessor {

    fun findReplacement(symbol: KSFunctionDeclaration, metadata: List<RichKotlinClassMetadata>): RewriteFindReplace {
        val functionName = symbol.simpleName.asString()
        val ksParentDeclaration = symbol.parentDeclaration
        val declaringParentName: String = if (ksParentDeclaration == null) {
            // Top level
            val kmDeclaringFacades = metadata.filter { metadata ->
                val (_, facade) = metadata
                if (facade !is KotlinClassMetadata.FileFacade) return@filter false

                facade.kmPackage.functions.any { it.name == functionName }
            }.distinctBy { it.packageName }

            if (kmDeclaringFacades.size == 1) {
                kmDeclaringFacades[0].packageName
            } else if (kmDeclaringFacades.isEmpty()) {
                error("Could not find file facade with function '${functionName}' (L${(symbol.location as FileLocation).lineNumber}) from '${symbol.packageName.asString()}.${symbol.containingFile!!.fileName}'")
            } else {
                error("There was multiple file facades with function '${functionName}' (L${(symbol.location as FileLocation).lineNumber}) from '${symbol.packageName.asString()}.${symbol.containingFile!!.fileName}': ${kmDeclaringFacades.joinToString { it.packageName }}")
            }
        } else if (ksParentDeclaration is KSClassDeclaration) {
            // Member
            error("jda-ktx doesn't have member functions to specifically replace, annotate the class instead")
        } else {
            error("Unexpected parent declaration: $ksParentDeclaration")
        }

        return RewriteFindReplace.Companion.from(symbol, "${declaringParentName}.${functionName}", symbol.qualifiedName!!.asString())
    }
}
