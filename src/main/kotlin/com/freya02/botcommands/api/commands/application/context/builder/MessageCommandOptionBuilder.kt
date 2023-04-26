package com.freya02.botcommands.api.commands.application.context.builder

import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandOptionBuilder
import kotlin.reflect.KFunction

class MessageCommandOptionBuilder(owner: KFunction<*>, declaredName: String) : ApplicationCommandOptionBuilder(owner, declaredName, declaredName)
