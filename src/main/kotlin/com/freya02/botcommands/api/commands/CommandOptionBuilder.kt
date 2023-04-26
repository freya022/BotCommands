package com.freya02.botcommands.api.commands

import com.freya02.botcommands.api.core.options.builder.OptionBuilder
import kotlin.reflect.KFunction

abstract class CommandOptionBuilder(owner: KFunction<*>, declaredName: String, val optionName: String) : OptionBuilder(owner, declaredName)
