package dev.freya02.botcommands.jda.ktx.deprecation.bc.core.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class DeprecationProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return DeprecationProcessor(
            environment.logger,
            environment.codeGenerator
        )
    }
}
