package com.freya02.botcommands.api.builder

import com.freya02.botcommands.api.application.builder.OptionBuilder
import com.freya02.botcommands.api.application.slash.GeneratedValueSupplier

//TODO transform into a GenerateMethodParameter factory
class GeneratedOptionBuilder(declaredName: String, val generatedValueSupplier: GeneratedValueSupplier) : OptionBuilder(
    declaredName,
    declaredName
)
