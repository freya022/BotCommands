package dev.freya02.botcommands.jda.ktx.deprecation.ktx.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dev.freya02.botcommands.jda.ktx.deprecation.RewriteFindReplace
import dev.freya02.botcommands.jda.ktx.deprecation.utils.RichKotlinClassMetadata
import dev.freya02.botcommands.jda.ktx.deprecation.utils.createFindAndReplaceRecipe
import io.github.classgraph.ClassGraph
import kotlin.metadata.jvm.KotlinClassMetadata
import kotlin.metadata.jvm.Metadata

private const val REPLACE_JDA_KTX = "dev.freya02.botcommands.jda.ktx.ReplaceJdaKtx"

class JdaKtxMigrationProcessor(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {

    private val findReplacePairs = linkedSetOf<RewriteFindReplace>()

    @Suppress("UNCHECKED_CAST")
    override fun process(resolver: Resolver): List<KSAnnotated> {
        // @ReplaceJdaKtx -> Create find&replace, no accessor as we don't have the sources
        val symbols = resolver.getSymbolsWithAnnotation(REPLACE_JDA_KTX, inDepth = false).toList()
        if (symbols.isEmpty()) return emptyList()

        val metadata = context(logger) { loadMetadata() }

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

        return emptyList()
    }

    @Suppress("UNCHECKED_CAST")
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

    override fun finish() {
        require(findReplacePairs.isNotEmpty())
        codeGenerator.createNewFile(
            Dependencies(
                aggregating = true,
                sources = findReplacePairs.map { it.processedFile }.toTypedArray()
            ),
            "META-INF/rewrite",
            "jda-ktx-to-bc-jdk-ktx",
            extensionName = "yml"
        ).use { outputStream ->
            val ktxRecipe = createFindAndReplaceRecipe(
                name = "dev.freya02.MigrateFromJdaKtxToBcJdaKtx",
                description = "Migrates most jda-ktx extensions to BotCommands-jda-ktx, may require further adjustments",
                pairs = findReplacePairs,
            )

            outputStream.write((ktxRecipe + "\n").encodeToByteArray())
        }
    }
}
