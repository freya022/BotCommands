package dev.freya02.botcommands.jda.ktx.deprecation.processor.bc

import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dev.freya02.botcommands.jda.ktx.deprecation.CompatSourceFile
import dev.freya02.botcommands.jda.ktx.deprecation.FindReplacePairs
import dev.freya02.botcommands.jda.ktx.deprecation.render.*
import dev.freya02.botcommands.jda.ktx.deprecation.utils.KaAccessor
import dev.freya02.botcommands.jda.ktx.deprecation.utils.createFindAndReplaceRecipe
import dev.freya02.botcommands.jda.ktx.deprecation.utils.suffixIfNotEmpty
import ksp.org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import ksp.org.jetbrains.kotlin.analysis.api.symbols.KaNamedFunctionSymbol
import kotlin.io.path.Path
import kotlin.io.path.readText

private const val DEPRECATED_IN_BC_CORE = "dev.freya02.botcommands.jda.ktx.DeprecatedInBcCore"
private const val UTILS_PACKAGE = "io.github.freya022.botcommands.api.core.utils"

class DeprecationProcessor(
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {

    private val sourceFiles = arrayListOf<CompatSourceFile>()
    private val findReplacePairs = FindReplacePairs()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        // @DeprecatedInBcCore -> Create accessors and find&replace
        resolver
            .getSymbolsWithAnnotation(DEPRECATED_IN_BC_CORE, inDepth = false)
            .groupBy { it.containingFile!! }
            .onEach { (containingFile, deprecatedNodes) ->

                val imports = mutableSetOf<String>()
                val functionAccessors = mutableListOf<String>()
                val typeAliases = mutableListOf<String>()

                context(imports, findReplacePairs) {
                    deprecatedNodes.forEach { node ->
                        when (node) {
                            is KSFunctionDeclaration -> {
                                functionAccessors += createFunctionAccessor(node)
                                    .lines()
                                    .joinToString(separator = "\n") { it.trimEnd() }
                            }

                            is KSClassDeclaration -> {
                                typeAliases += createTypeAlias(node)
                            }

                            else -> error("Unhandled node: $node")
                        }
                    }
                }

                sourceFiles += CompatSourceFile(
                    containingFile,
                    UTILS_PACKAGE,
                    "${containingFile.fileName.removeSuffix(".kt")}Compat",
                    generateSource(containingFile, imports, typeAliases, functionAccessors)
                )
            }

        return emptyList()
    }

    override fun finish() {
        require(sourceFiles.isNotEmpty())
        sourceFiles.forEach { (processedFile, packageName, fileName, content) ->
            codeGenerator.createNewFile(
                Dependencies(
                    aggregating = true,
                    sources = arrayOf(processedFile)
                ),
                packageName = packageName,
                fileName = fileName
            ).use { outputStream ->
                outputStream.write(content.encodeToByteArray())
            }
        }

        require(findReplacePairs.pairs.isNotEmpty())
        codeGenerator.createNewFile(
            Dependencies(
                aggregating = true,
                sources = findReplacePairs.processedFiles.toTypedArray()
            ),
            "META-INF/rewrite",
            "bc-core-to-bc-jdk-ktx",
            extensionName = "yml"
        ).use { outputStream ->
            val recipe = createFindAndReplaceRecipe(
                name = "dev.freya02.MigrateFromBcCoreToBcJdaKtx",
                description = "Migrates most BotCommands-core extensions to BotCommands-jda-ktx, may require further adjustments",
                pairs = findReplacePairs,
            )

            outputStream.write((recipe + "\n").encodeToByteArray())
        }
    }

    private fun generateSource(
        containingFile: KSFile,
        imports: Set<String>,
        classTexts: List<String>,
        functionTexts: List<String>,
    ): String = buildString {
        append("""
            @file:Suppress("unused")
            
            package $UTILS_PACKAGE
        """.trimIndent())

        appendLine()
        appendLine()

        val existingImports = Path(containingFile.filePath).readText().lines()
            .filter { it.startsWith("import") }
            .filterNot { it.endsWith("DeprecatedInBcCore") }
        if (existingImports.isNotEmpty()) {
            appendLine(existingImports.joinToString("\n"))
            appendLine()
        }
        if (imports.isNotEmpty()) {
            appendLine(imports.joinToString("\n") { "import $it" })
            appendLine()
        }
        if (classTexts.isNotEmpty()) {
            appendLine(classTexts.joinToString("\n\n"))
            appendLine()
        }
        if (functionTexts.isNotEmpty()) {
            appendLine(functionTexts.joinToString("\n\n"))
            appendLine()
        }
    }.trimEnd('\n') + '\n'

    @OptIn(KaExperimentalApi::class)
    context(imports: MutableSet<String>, findReplacePairs: FindReplacePairs)
    private fun createFunctionAccessor(function: KSFunctionDeclaration): String {
        val fullOldFunction = "$UTILS_PACKAGE.${function.simpleName.asString()}"
        val fullNewFunction = function.qualifiedName!!.asString()
        findReplacePairs.add(function, fullOldFunction, fullNewFunction)

        val functionSymbol = KaAccessor.getKaFunctionSymbol(function) as KaNamedFunctionSymbol

        val docs = function.renderDocString()
        val annotations = buildList {
            if (functionSymbol.contractEffects.isNotEmpty()) {
                add("@OptIn(ExperimentalContracts::class)")
            }
            function.annotations.filterNot { it.shortName.asString() == "DeprecatedInBcCore" }.forEach { annotation ->
                add(annotation.render())
            }
        }.joinToString(separator = "\n")
        val modifiers = function.renderModifiers().suffixIfNotEmpty(" ")
        val typeParameters = function.renderTypeParameters().suffixIfNotEmpty(" ")
        val extensionReceiver = function.renderExtensionReceiver()
        val functionName = function.simpleName.asString()
        val parameters = function.renderParameters()
        val returnType = function.returnType!!.resolve().toString()
        val contract = functionSymbol.renderContract()
        val delegateExpr = run {
            val delegateName = "${function.simpleName.asString()}_"
            imports += function.qualifiedName!!.asString() + " as $delegateName"
            val arguments = function.parameters.joinToString(", ") {
                val paramName = it.name!!.asString()
                "$paramName = $paramName"
            }
            "return $delegateName($arguments)"
        }

        return buildString {
            appendLine(docs)
            if (annotations.isNotEmpty()) appendLine(annotations)
            if (annotations.lines().none { it.startsWith("@Deprecated") }) {
                appendLine("""
                    @Deprecated(
                        message = "Moved to the BotCommands-jda-ktx module\n" +
                                  "You can find & replace:\n" +
                                  "Find: $fullOldFunction\n" + 
                                  "Replace: $fullNewFunction" 
                    )
                """.trimIndent())
            }
            appendLine("${modifiers}fun ${typeParameters}${extensionReceiver}${functionName}${parameters}: $returnType {")
            if (contract.isNotEmpty())
                append(contract.prependIndent()).append("\n\n")
            appendLine(delegateExpr.prependIndent())

            append("}")
        }
    }

    context(findReplacePairs: FindReplacePairs)
    private fun createTypeAlias(clazz: KSClassDeclaration): String {
        val aliasName = clazz.simpleName.asString()
        val implFullName = clazz.qualifiedName!!.asString()
        val typeArguments = clazz.renderTypeParameters()

        findReplacePairs.add(clazz, "$UTILS_PACKAGE.$aliasName", implFullName)

        return "typealias $aliasName$typeArguments = $implFullName$typeArguments"
    }
}
