package com.freya02.botcommands.internal.commands.application

import com.freya02.botcommands.api.commands.application.builder.ApplicationGeneratedOptionBuilder
import com.freya02.botcommands.internal.commands.GeneratedOption
import com.freya02.botcommands.internal.core.options.OptionImpl
import com.freya02.botcommands.internal.core.options.OptionType

class ApplicationGeneratedOption(
    generatedOptionBuilder: ApplicationGeneratedOptionBuilder
) : OptionImpl(generatedOptionBuilder.optionParameter, OptionType.GENERATED), GeneratedOption {
    val generatedValueSupplier = generatedOptionBuilder.generatedValueSupplier
}
