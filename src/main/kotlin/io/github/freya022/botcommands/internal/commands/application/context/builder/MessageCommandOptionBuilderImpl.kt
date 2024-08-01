package io.github.freya022.botcommands.internal.commands.application.context.builder

import io.github.freya022.botcommands.api.commands.application.context.builder.MessageCommandOptionBuilder
import io.github.freya022.botcommands.internal.commands.application.builder.ApplicationCommandOptionBuilderImpl
import io.github.freya022.botcommands.internal.parameters.OptionParameter

internal class MessageCommandOptionBuilderImpl internal constructor(
    optionParameter: OptionParameter,
) : ApplicationCommandOptionBuilderImpl(optionParameter),
    MessageCommandOptionBuilder