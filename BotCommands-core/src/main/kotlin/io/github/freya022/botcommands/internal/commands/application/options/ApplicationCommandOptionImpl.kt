package io.github.freya022.botcommands.internal.commands.application.options

import io.github.freya022.botcommands.api.commands.application.options.ApplicationCommandOption
import io.github.freya022.botcommands.internal.commands.application.options.builder.ApplicationCommandOptionBuilderImpl
import io.github.freya022.botcommands.internal.commands.options.CommandOptionImpl

internal abstract class ApplicationCommandOptionImpl internal constructor(
    optionBuilder: ApplicationCommandOptionBuilderImpl
) : CommandOptionImpl(optionBuilder),
    ApplicationCommandOption