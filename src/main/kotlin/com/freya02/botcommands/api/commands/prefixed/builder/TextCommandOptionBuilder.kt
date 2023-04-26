package com.freya02.botcommands.api.commands.prefixed.builder

import com.freya02.botcommands.api.commands.CommandOptionBuilder
import kotlin.reflect.KFunction

class TextCommandOptionBuilder internal constructor(owner: KFunction<*>, declaredName: String, optionName: String) : CommandOptionBuilder(owner, declaredName, optionName) {
    var helpExample: String? = null
    var isId: Boolean = false
}
