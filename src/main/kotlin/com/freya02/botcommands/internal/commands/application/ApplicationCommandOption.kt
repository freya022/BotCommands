package com.freya02.botcommands.internal.commands.application

import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandOptionBuilder

abstract class ApplicationCommandOption(
    optionBuilder: ApplicationCommandOptionBuilder
) : CommandOption(optionBuilder)