package com.freya02.botcommands.api.builder

import com.freya02.botcommands.api.application.builder.OptionBuilder
import com.freya02.botcommands.api.application.slash.GeneratedValueSupplier

class GeneratedOptionBuilder(declaredName: String, val generatedValueSupplier: GeneratedValueSupplier) : OptionBuilder(
    declaredName,
    declaredName
)
