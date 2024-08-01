package io.github.freya022.botcommands.internal.commands.application

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandOption
import io.github.freya022.botcommands.internal.commands.CommandOptionImpl
import io.github.freya022.botcommands.internal.commands.application.builder.ApplicationCommandOptionBuilderImpl

internal abstract class ApplicationCommandOptionImpl internal constructor(
    optionBuilder: ApplicationCommandOptionBuilderImpl
) : CommandOptionImpl(optionBuilder),
    ApplicationCommandOption