package io.github.freya022.botcommands.internal.commands.application.options

import io.github.freya022.botcommands.api.commands.application.options.ApplicationCommandParameter
import io.github.freya022.botcommands.internal.commands.application.options.builder.ApplicationGeneratedOptionBuilderImpl
import io.github.freya022.botcommands.internal.core.options.AbstractGeneratedOption

internal class ApplicationGeneratedOption internal constructor(
    override val parent: ApplicationCommandParameter,
    generatedOptionBuilder: ApplicationGeneratedOptionBuilderImpl
) : AbstractGeneratedOption(generatedOptionBuilder.optionParameter) {

    override val executable get() = parent.executable

    val generatedValueSupplier = generatedOptionBuilder.generatedValueSupplier
}
