package io.github.freya022.botcommands.internal.commands.application.options.builder

import io.github.freya022.botcommands.api.commands.application.options.builder.ApplicationCommandOptionBuilder
import io.github.freya022.botcommands.internal.commands.options.builder.CommandOptionBuilderImpl
import io.github.freya022.botcommands.internal.parameters.OptionParameter

internal abstract class ApplicationCommandOptionBuilderImpl internal constructor(
    optionParameter: OptionParameter,
) : CommandOptionBuilderImpl(optionParameter),
    ApplicationCommandOptionBuilder