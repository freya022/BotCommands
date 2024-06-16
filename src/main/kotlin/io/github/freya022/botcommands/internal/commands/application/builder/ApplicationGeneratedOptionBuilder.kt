package io.github.freya022.botcommands.internal.commands.application.builder

import io.github.freya022.botcommands.api.commands.application.ApplicationGeneratedValueSupplier
import io.github.freya022.botcommands.internal.commands.application.ApplicationGeneratedOption
import io.github.freya022.botcommands.internal.core.options.builder.AbstractGeneratedOptionBuilder
import io.github.freya022.botcommands.internal.parameters.OptionParameter

internal class ApplicationGeneratedOptionBuilder internal constructor(
    optionParameter: OptionParameter,
    internal val generatedValueSupplier: ApplicationGeneratedValueSupplier
) : AbstractGeneratedOptionBuilder(optionParameter) {
    override fun toGeneratedOption() = ApplicationGeneratedOption(this)
}
