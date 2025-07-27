package dev.freya02.botcommands.jda.ktx.deprecation.processor.bc

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class DeprecationProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return DeprecationProcessor(
            environment.codeGenerator
        )
    }
}
