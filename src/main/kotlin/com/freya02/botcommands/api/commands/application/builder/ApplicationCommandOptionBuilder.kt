package com.freya02.botcommands.api.commands.application.builder

import com.freya02.botcommands.api.commands.CommandOptionBuilder
import com.freya02.botcommands.internal.parameters.OptionParameter

//TODO move optionName to SlashCommandOptionBuilder instead, remove this layer
abstract class ApplicationCommandOptionBuilder(optionParameter: OptionParameter, optionName: String) : CommandOptionBuilder(optionParameter, optionName)
