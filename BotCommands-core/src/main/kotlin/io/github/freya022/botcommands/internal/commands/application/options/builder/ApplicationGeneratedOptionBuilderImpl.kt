package io.github.freya022.botcommands.internal.commands.application.options.builder

import io.github.freya022.botcommands.api.commands.application.ApplicationGeneratedValueSupplier
import io.github.freya022.botcommands.api.commands.application.options.ApplicationCommandParameter
import io.github.freya022.botcommands.api.parameters.AggregatedParameter
import io.github.freya022.botcommands.internal.commands.application.options.ApplicationGeneratedOption
import io.github.freya022.botcommands.internal.core.options.builder.AbstractGeneratedOptionBuilderImpl
import io.github.freya022.botcommands.internal.parameters.OptionParameter

internal class ApplicationGeneratedOptionBuilderImpl internal constructor(
    optionParameter: OptionParameter,
    internal val generatedValueSupplier: ApplicationGeneratedValueSupplier
) : AbstractGeneratedOptionBuilderImpl(optionParameter) {
    override fun toGeneratedOption(parent: AggregatedParameter) =
        ApplicationGeneratedOption(parent as ApplicationCommandParameter, this)
}
