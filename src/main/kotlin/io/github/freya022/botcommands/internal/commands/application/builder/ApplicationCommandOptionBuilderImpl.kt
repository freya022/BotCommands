package io.github.freya022.botcommands.internal.commands.application.builder

import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandOptionBuilder
import io.github.freya022.botcommands.internal.commands.builder.CommandOptionBuilderImpl
import io.github.freya022.botcommands.internal.parameters.OptionParameter

internal abstract class ApplicationCommandOptionBuilderImpl internal constructor(
    optionParameter: OptionParameter,
) : CommandOptionBuilderImpl(optionParameter),
    ApplicationCommandOptionBuilder