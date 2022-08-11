package com.freya02.botcommands.api.builder

import com.freya02.botcommands.api.application.builder.OptionBuilder
import com.freya02.botcommands.api.application.slash.GeneratedValueSupplier

class GeneratedOptionBuilder(name: String, val generatedValueSupplier: GeneratedValueSupplier) : OptionBuilder(name)
