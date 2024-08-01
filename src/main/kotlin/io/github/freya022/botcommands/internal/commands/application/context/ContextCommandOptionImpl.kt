package io.github.freya022.botcommands.internal.commands.application.context

import io.github.freya022.botcommands.api.commands.application.context.ContextCommandOption
import io.github.freya022.botcommands.internal.commands.application.builder.ApplicationCommandOptionBuilderImpl
import io.github.freya022.botcommands.internal.core.options.OptionImpl
import io.github.freya022.botcommands.internal.core.options.OptionType

internal abstract class ContextCommandOptionImpl internal constructor(
    optionBuilder: ApplicationCommandOptionBuilderImpl
) : OptionImpl(optionBuilder.optionParameter, OptionType.OPTION),
    ContextCommandOption