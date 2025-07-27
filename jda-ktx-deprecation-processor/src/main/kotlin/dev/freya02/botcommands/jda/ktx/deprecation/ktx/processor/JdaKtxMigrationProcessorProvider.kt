package dev.freya02.botcommands.jda.ktx.deprecation.ktx.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class JdaKtxMigrationProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return JdaKtxMigrationProcessor(
            environment.logger,
            environment.codeGenerator
        )
    }
}
