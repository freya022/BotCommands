package com.freya02.botcommands.api.commands.application.builder

import com.freya02.botcommands.api.commands.CommandOptionBuilder
import kotlin.reflect.KFunction

abstract class ApplicationCommandOptionBuilder(owner: KFunction<*>, declaredName: String, optionName: String) : CommandOptionBuilder(owner, declaredName, optionName)
