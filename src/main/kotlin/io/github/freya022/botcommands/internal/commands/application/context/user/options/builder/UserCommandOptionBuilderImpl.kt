package io.github.freya022.botcommands.internal.commands.application.context.user.options.builder

import io.github.freya022.botcommands.api.commands.application.context.user.options.builder.UserCommandOptionBuilder
import io.github.freya022.botcommands.internal.commands.application.options.builder.ApplicationCommandOptionBuilderImpl
import io.github.freya022.botcommands.internal.parameters.OptionParameter

internal class UserCommandOptionBuilderImpl internal constructor(
    optionParameter: OptionParameter,
) : ApplicationCommandOptionBuilderImpl(optionParameter),
    UserCommandOptionBuilder