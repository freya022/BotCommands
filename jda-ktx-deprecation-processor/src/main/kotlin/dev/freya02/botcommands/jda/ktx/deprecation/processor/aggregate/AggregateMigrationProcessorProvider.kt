package dev.freya02.botcommands.jda.ktx.deprecation.processor.aggregate

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class AggregateMigrationProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return AggregateMigrationProcessor(
            environment.codeGenerator,
        )
    }
}
