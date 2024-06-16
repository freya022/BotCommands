package io.github.freya022.botcommands.internal.commands.application

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandOption
import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandOptionBuilder
import io.github.freya022.botcommands.internal.commands.CommandOptionImpl

internal abstract class ApplicationCommandOptionImpl internal constructor(
    optionBuilder: ApplicationCommandOptionBuilder
) : CommandOptionImpl(optionBuilder),
    ApplicationCommandOption