package dev.freya02.botcommands.jda.ktx.deprecation.ktx.processor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import dev.freya02.botcommands.jda.ktx.deprecation.RewriteFindReplace
import dev.freya02.botcommands.jda.ktx.deprecation.utils.RichKotlinClassMetadata
import kotlin.metadata.jvm.KotlinClassMetadata

object JdaKtxClassProcessor {

    fun findReplacement(symbol: KSClassDeclaration, metadata: List<RichKotlinClassMetadata>): RewriteFindReplace {
        val ktxFullName = metadata.firstNotNullOfOrNull { metadata ->
            val (_, clazz) = metadata
            if (clazz !is KotlinClassMetadata.Class) return@firstNotNullOfOrNull null

            val targetSymbolFullName = clazz.kmClass.name.replace('/', '.')
            val targetSymbolSimpleName = targetSymbolFullName.dropWhile { !it.isUpperCase() }
            if (symbol.simpleName.asString() == targetSymbolSimpleName) {
                targetSymbolFullName
            } else {
                null
            }
        } ?: error("Could not find jda-ktx metadata for symbol ${symbol.qualifiedName!!.asString()}")

        return RewriteFindReplace.Companion.from(symbol, ktxFullName, symbol.qualifiedName!!.asString())
    }
}
