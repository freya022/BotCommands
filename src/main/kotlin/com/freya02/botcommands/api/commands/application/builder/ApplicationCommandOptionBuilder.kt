package com.freya02.botcommands.api.commands.application.builder

import kotlin.reflect.KFunction

abstract class ApplicationCommandOptionBuilder(owner: KFunction<*>, declaredName: String, optionName: String) : OptionBuilder(owner, declaredName, optionName)
