package io.github.freya022.botcommands.internal.commands.application.context.options

import io.github.freya022.botcommands.api.commands.application.context.options.ContextCommandOption
import io.github.freya022.botcommands.internal.commands.application.options.builder.ApplicationCommandOptionBuilderImpl
import io.github.freya022.botcommands.internal.core.options.OptionImpl
import io.github.freya022.botcommands.internal.core.options.OptionType

internal abstract class ContextCommandOptionImpl internal constructor(
    optionBuilder: ApplicationCommandOptionBuilderImpl
) : OptionImpl(optionBuilder.optionParameter, OptionType.OPTION),
    ContextCommandOption