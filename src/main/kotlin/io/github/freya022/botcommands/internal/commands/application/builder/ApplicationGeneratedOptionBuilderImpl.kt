package io.github.freya022.botcommands.internal.commands.application.builder

import io.github.freya022.botcommands.api.commands.application.ApplicationGeneratedValueSupplier
import io.github.freya022.botcommands.internal.commands.application.ApplicationGeneratedOption
import io.github.freya022.botcommands.internal.core.options.builder.AbstractGeneratedOptionBuilderImpl
import io.github.freya022.botcommands.internal.parameters.OptionParameter

internal class ApplicationGeneratedOptionBuilderImpl internal constructor(
    optionParameter: OptionParameter,
    internal val generatedValueSupplier: ApplicationGeneratedValueSupplier
) : AbstractGeneratedOptionBuilderImpl(optionParameter) {
    override fun toGeneratedOption() = ApplicationGeneratedOption(this)
}
