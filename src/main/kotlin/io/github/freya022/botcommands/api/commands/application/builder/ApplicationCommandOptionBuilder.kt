package io.github.freya022.botcommands.api.commands.application.builder

import io.github.freya022.botcommands.api.commands.CommandOptionBuilder
import io.github.freya022.botcommands.internal.parameters.OptionParameter

abstract class ApplicationCommandOptionBuilder internal constructor(optionParameter: OptionParameter) : CommandOptionBuilder(optionParameter)
