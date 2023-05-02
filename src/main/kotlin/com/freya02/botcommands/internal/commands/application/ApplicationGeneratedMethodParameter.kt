package com.freya02.botcommands.internal.commands.application

import com.freya02.botcommands.api.commands.application.builder.ApplicationGeneratedOptionBuilder
import com.freya02.botcommands.internal.commands.GeneratedMethodParameter
import com.freya02.botcommands.internal.core.options.AbstractOptionImpl
import com.freya02.botcommands.internal.core.options.OptionType

class ApplicationGeneratedMethodParameter(
    generatedOptionBuilder: ApplicationGeneratedOptionBuilder
) : AbstractOptionImpl(generatedOptionBuilder.optionParameter, OptionType.GENERATED), GeneratedMethodParameter {
    val generatedValueSupplier = generatedOptionBuilder.generatedValueSupplier
}
