package io.github.freya022.botcommands.internal.commands.application

import io.github.freya022.botcommands.internal.commands.application.builder.ApplicationGeneratedOptionBuilderImpl
import io.github.freya022.botcommands.internal.core.options.AbstractGeneratedOption

internal class ApplicationGeneratedOption internal constructor(
    generatedOptionBuilder: ApplicationGeneratedOptionBuilderImpl
) : AbstractGeneratedOption(generatedOptionBuilder.optionParameter) {
    val generatedValueSupplier = generatedOptionBuilder.generatedValueSupplier
}
