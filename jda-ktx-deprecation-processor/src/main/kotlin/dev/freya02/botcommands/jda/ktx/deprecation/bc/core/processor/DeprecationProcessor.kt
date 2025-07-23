package dev.freya02.botcommands.jda.ktx.deprecation.bc.core.processor

import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dev.freya02.botcommands.jda.ktx.deprecation.CompatSourceFile
import dev.freya02.botcommands.jda.ktx.deprecation.RewriteFindReplace
import dev.freya02.botcommands.jda.ktx.deprecation.ktx.processor.JdaKtxMigrationProcessor
import dev.freya02.botcommands.jda.ktx.deprecation.render.*
import dev.freya02.botcommands.jda.ktx.deprecation.utils.KaAccessor
import dev.freya02.botcommands.jda.ktx.deprecation.utils.suffixIfNotEmpty
import ksp.org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import ksp.org.jetbrains.kotlin.analysis.api.symbols.KaNamedFunctionSymbol
import kotlin.io.path.Path
import kotlin.io.path.readText

private const val REPLACE_JDA_KTX = "dev.freya02.botcommands.jda.ktx.ReplaceJdaKtx"
private const val DEPRECATED_IN_BC_CORE = "dev.freya02.botcommands.jda.ktx.DeprecatedInBcCore"
private const val UTILS_PACKAGE = "io.github.freya022.botcommands.api.core.utils"

class DeprecationProcessor(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {

    private val sourceFiles = arrayListOf<CompatSourceFile>()
    private val ktxFindReplacePairs = linkedSetOf<RewriteFindReplace>()
    private val bcCoreFindReplacePairs = linkedSetOf<RewriteFindReplace>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        // @ReplaceJdaKtx -> Create find&replace, no accessor as we don't have the sources
        context(logger, codeGenerator, ktxFindReplacePairs) {
            val jdaKtxReplacedSymbols = resolver.getSymbolsWithAnnotation(REPLACE_JDA_KTX, inDepth = false).toList()
            JdaKtxMigrationProcessor.process(jdaKtxReplacedSymbols)
        }

        // @DeprecatedInBcCore -> Create accessors and find&replace
        resolver
            .getSymbolsWithAnnotation(DEPRECATED_IN_BC_CORE, inDepth = false)
            .groupBy { it.containingFile!! }
            .onEach { (containingFile, deprecatedNodes) ->

                val imports = mutableSetOf<String>()
                val functionAccessors = mutableListOf<String>()
                val typeAliases = mutableListOf<String>()

                context(imports, bcCoreFindReplacePairs) {
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

        require(ktxFindReplacePairs.isNotEmpty() && bcCoreFindReplacePairs.isNotEmpty())
        codeGenerator.createNewFile(
            Dependencies(
                aggregating = true,
                sources = (ktxFindReplacePairs + bcCoreFindReplacePairs).map { it.processedFile }.toTypedArray()
            ),
            "META-INF/rewrite",
            "rewrite",
            extensionName = "yml"
        ).use { outputStream ->
            val aggregateRecipe = """
                ---
                type: specs.openrewrite.org/v1beta/recipe
                name: dev.freya02.MigrateToBcJdaKtx
                description: Migrates most jda-ktx and BotCommands-core extensions to BotCommands-jda-ktx, may require further adjustments
                recipeList:
                  - dev.freya02.MigrateFromJdaKtxToBcJdaKtx
                  - dev.freya02.MigrateFromBcCoreToBcJdaKtx
            """.trimIndent()
            val ktxRecipe = createFindAndReplaceRecipe(
                name = "dev.freya02.MigrateFromJdaKtxToBcJdaKtx",
                description = "Migrates most jda-ktx extensions to BotCommands-jda-ktx, may require further adjustments",
                pairs = ktxFindReplacePairs,
            )
            val bcCoreRecipe = createFindAndReplaceRecipe(
                name = "dev.freya02.MigrateFromBcCoreToBcJdaKtx",
                description = "Migrates most BotCommands-core extensions to BotCommands-jda-ktx, may require further adjustments",
                pairs = bcCoreFindReplacePairs,
            )

            outputStream.write((aggregateRecipe + "\n\n" + ktxRecipe + "\n\n" + bcCoreRecipe + "\n").encodeToByteArray())
        }
    }

    private fun createFindAndReplaceRecipe(name: String, description: String, pairs: Collection<RewriteFindReplace>): String {
        val header = """
            ---
            type: specs.openrewrite.org/v1beta/recipe
            name: $name
            description: $description
            recipeList:
        """.trimIndent()

        val recipes = pairs.withStarImports().joinToString("\n") { (_, old, new) ->
            """
                - org.openrewrite.text.FindAndReplace:
                    find: "$old"
                    replace: "$new"
            """.trimIndent().prependIndent("  ")
        }

        return header + "\n" + recipes
    }

    private fun Collection<RewriteFindReplace>.withStarImports(): List<RewriteFindReplace> {
        val added = hashSetOf<Pair<String, String>>()

        return flatMap { rule ->
            fun String.getPackage(): String {
                // Assume there are no rule with nested classes, so we can just drop the last import component
                return substringBeforeLast('.')
            }

            val oldPackage = rule.old.getPackage()
            val newPackage = rule.new.getPackage()
            if (added.add(oldPackage to newPackage)) {
                listOf(rule, rule.copy(old = "$oldPackage.*", new = "$oldPackage.*\\nimport $newPackage.*"))
            } else {
                listOf(rule)
            }
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
    context(imports: MutableSet<String>, findReplacePairs: MutableCollection<RewriteFindReplace>)
    private fun createFunctionAccessor(function: KSFunctionDeclaration): String {
        val fullOldFunction = "$UTILS_PACKAGE.${function.simpleName.asString()}"
        val fullNewFunction = function.qualifiedName!!.asString()
        findReplacePairs +=  RewriteFindReplace.Companion.from(function, fullOldFunction, fullNewFunction)

        val functionSymbol = KaAccessor.getKaFunctionSymbol(function) as KaNamedFunctionSymbol

        val docs = function.renderDocString()
        val annotations = buildList {
            if (functionSymbol.contractEffects.isNotEmpty()) {
                add("@OptIn(ExperimentalContracts::class)")
            }
            function.annotations.filterNot { it.shortName.asString() == "DeprecatedInBcCore" }.forEach { annotation ->
                add("@${annotation.shortName.asString()}(${annotation.arguments.joinToString(", ") { getCompileValue(it.value) }})")
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
            appendLine("""
                @Deprecated(
                    message = "Moved to the BotCommands-jda-ktx module\n" +
                              "You can find & replace:\n" +
                              "Find: $fullOldFunction\n" + 
                              "Replace: $fullOldFunction" 
                )
            """.trimIndent())
            appendLine("${modifiers}fun ${typeParameters}${extensionReceiver}${functionName}${parameters}: $returnType {")
            if (contract.isNotEmpty())
                append(contract.prependIndent()).append("\n\n")
            appendLine(delegateExpr.prependIndent())

            append("}")
        }
    }

    private fun getCompileValue(value: Any?): String {
        if (value is List<*>)
            return value.joinToString { getCompileValue(it) }

        return when (value) {
            is String -> "\"$value\""
            else -> error("Unsupported annotation value $value")
        }
    }

    context(findReplacePairs: MutableCollection<RewriteFindReplace>)
    private fun createTypeAlias(clazz: KSClassDeclaration): String {
        val aliasName = clazz.simpleName.asString()
        val implFullName = clazz.qualifiedName!!.asString()
        val typeArguments = clazz.renderTypeParameters()

        findReplacePairs +=  RewriteFindReplace.Companion.from(clazz, "$UTILS_PACKAGE.$aliasName", implFullName)

        return "typealias $aliasName$typeArguments = $implFullName$typeArguments"
    }
}
