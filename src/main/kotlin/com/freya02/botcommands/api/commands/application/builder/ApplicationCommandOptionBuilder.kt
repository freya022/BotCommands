package com.freya02.botcommands.api.commands.application.builder

import com.freya02.botcommands.api.commands.CommandOptionBuilder
import com.freya02.botcommands.internal.parameters.MultiParameter

//TODO move optionName to SlashCommandOptionBuilder instead, remove this layer
abstract class ApplicationCommandOptionBuilder(multiParameter: MultiParameter, optionName: String) : CommandOptionBuilder(multiParameter, optionName)
