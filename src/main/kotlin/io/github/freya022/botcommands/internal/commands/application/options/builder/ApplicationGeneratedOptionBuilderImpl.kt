package io.github.freya022.botcommands.internal.commands.application.options.builder

import io.github.freya022.botcommands.api.commands.application.ApplicationGeneratedValueSupplier
import io.github.freya022.botcommands.api.core.Executable
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.application.options.ApplicationGeneratedOption
import io.github.freya022.botcommands.internal.core.options.builder.AbstractGeneratedOptionBuilderImpl
import io.github.freya022.botcommands.internal.parameters.OptionParameter

internal class ApplicationGeneratedOptionBuilderImpl internal constructor(
    optionParameter: OptionParameter,
    internal val generatedValueSupplier: ApplicationGeneratedValueSupplier
) : AbstractGeneratedOptionBuilderImpl(optionParameter) {
    override fun toGeneratedOption(executable: Executable) =
        ApplicationGeneratedOption(executable as ApplicationCommandInfoImpl, this)
}
