package dev.freya02.botcommands.jda.ktx.deprecation.processor.aggregate

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated

class AggregateMigrationProcessor(
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        return emptyList()
    }

    override fun finish() {
        codeGenerator.createNewFile(
            Dependencies(
                aggregating = true,
                sources = emptyArray()
            ),
            "META-INF/rewrite",
            "migrate-to-bc-jda-ktx",
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

            outputStream.write(aggregateRecipe.encodeToByteArray())
        }
    }
}
