package io.github.freya022.botcommands.internal.commands.application

import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandOptionBuilder

abstract class ApplicationCommandOption(
    optionBuilder: ApplicationCommandOptionBuilder
) : CommandOption(optionBuilder)