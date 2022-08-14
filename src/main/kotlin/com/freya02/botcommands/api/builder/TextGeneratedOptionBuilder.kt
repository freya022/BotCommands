package com.freya02.botcommands.api.builder

import com.freya02.botcommands.api.application.builder.OptionBuilder
import com.freya02.botcommands.api.prefixed.TextGeneratedValueSupplier

class TextGeneratedOptionBuilder(declaredName: String, val generatedValueSupplier: TextGeneratedValueSupplier) : OptionBuilder(
    declaredName,
    declaredName
)
