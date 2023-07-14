package com.freya02.botcommands.api.commands.application.builder

import com.freya02.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier
import com.freya02.botcommands.api.commands.builder.GeneratedOptionBuilder
import com.freya02.botcommands.api.core.options.builder.OptionBuilder
import com.freya02.botcommands.internal.commands.GeneratedOption
import com.freya02.botcommands.internal.commands.application.ApplicationGeneratedOption
import com.freya02.botcommands.internal.parameters.OptionParameter

class ApplicationGeneratedOptionBuilder internal constructor(
    optionParameter: OptionParameter,
    val generatedValueSupplier: ApplicationGeneratedValueSupplier
) : OptionBuilder(optionParameter), GeneratedOptionBuilder {
    override fun toGeneratedOption(): GeneratedOption =
        ApplicationGeneratedOption(this)
}
