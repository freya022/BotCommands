package dev.freya02.botcommands.jda.ktx.deprecation.ktx.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dev.freya02.botcommands.jda.ktx.deprecation.RewriteFindReplace
import dev.freya02.botcommands.jda.ktx.deprecation.utils.RichKotlinClassMetadata
import io.github.classgraph.ClassGraph
import kotlin.metadata.jvm.KotlinClassMetadata
import kotlin.metadata.jvm.Metadata

object JdaKtxMigrationProcessor {

    @Suppress("UNCHECKED_CAST")
    context(logger: KSPLogger, codeGenerator: CodeGenerator, findReplacePairs: MutableCollection<RewriteFindReplace>)
    fun process(symbols: List<KSAnnotated>) {
        if (symbols.isEmpty()) return

        val metadata = loadMetadata()

        for (symbol in symbols) {
            val annotation = symbol.annotations.first { it.shortName.asString() == "ReplaceJdaKtx" }
            val forcedPkg = annotation.arguments.first { it.name?.asString() == "pkg" }.value as String

            when (symbol) {
                is KSClassDeclaration -> {
                    findReplacePairs += if (forcedPkg.isNotBlank()) {
                        RewriteFindReplace.Companion.from(symbol, forcedPkg + "." + symbol.simpleName.asString(), symbol.qualifiedName!!.asString())
                    } else {
                        JdaKtxClassProcessor.findReplacement(symbol, metadata)
                    }
                }
                is KSFunctionDeclaration -> {
                    require(symbol.parentDeclaration == null) { "Only top-level functions are supported" }
                    findReplacePairs += if (forcedPkg.isNotBlank()) {
                        RewriteFindReplace.Companion.from(symbol, forcedPkg + "." + symbol.simpleName.asString(), symbol.qualifiedName!!.asString())
                    } else {
                        JdaKtxFunctionProcessor.findReplacement(symbol, metadata)
                    }
                }
                else -> {
                    logger.warn("Unhandled declaration: $symbol")
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    context(logger: KSPLogger)
    private fun loadMetadata(): List<RichKotlinClassMetadata> {
        return ClassGraph()
            .acceptPackages("dev.minn.jda.ktx")
            .enableAnnotationInfo()
            .scan()
            .use { scan ->
                buildList {
                    scan.allClasses.forEach { classInfo ->
                        val metadataAnnotation = classInfo.annotationInfo.directOnly().get(Metadata::class.java.name) ?: return@forEach
                        val values = metadataAnnotation.getParameterValues(true)

                        val metadata = KotlinClassMetadata.readStrict(
                            Metadata(
                                kind = values.getValue("k") as Int,
                                metadataVersion = values.getValue("mv") as IntArray,
                                data1 = values.getValue("d1") as Array<String>,
                                data2 = values.getValue("d2") as Array<String>,
                                extraString = values.getValue("xs") as String,
                                packageName = values.getValue("pn") as String,
                                extraInt = values.getValue("xi") as Int
                            )
                        )
                        if (metadata !is KotlinClassMetadata.Class && metadata !is KotlinClassMetadata.FileFacade) return@forEach

                        add(RichKotlinClassMetadata(classInfo.packageName, metadata))
                    }
                }
            }
    }
}
