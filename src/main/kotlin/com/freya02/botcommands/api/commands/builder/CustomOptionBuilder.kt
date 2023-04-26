package com.freya02.botcommands.api.commands.builder

import com.freya02.botcommands.api.commands.application.builder.OptionBuilder
import kotlin.reflect.KFunction

class CustomOptionBuilder(owner: KFunction<*>, declaredName: String) : OptionBuilder(owner, declaredName)