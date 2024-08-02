package io.github.freya022.botcommands.internal.commands.application.context.message.options.builder

import io.github.freya022.botcommands.api.commands.application.context.message.options.builder.MessageCommandOptionBuilder
import io.github.freya022.botcommands.internal.commands.application.options.builder.ApplicationCommandOptionBuilderImpl
import io.github.freya022.botcommands.internal.parameters.OptionParameter

internal class MessageCommandOptionBuilderImpl internal constructor(
    optionParameter: OptionParameter,
) : ApplicationCommandOptionBuilderImpl(optionParameter),
    MessageCommandOptionBuilder