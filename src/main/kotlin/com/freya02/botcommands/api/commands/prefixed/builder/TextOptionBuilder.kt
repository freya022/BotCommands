package com.freya02.botcommands.api.commands.prefixed.builder

import com.freya02.botcommands.api.commands.application.builder.OptionBuilder
import kotlin.reflect.KFunction

class TextOptionBuilder internal constructor(owner: KFunction<*>, declaredName: String, optionName: String) : OptionBuilder(owner, declaredName, optionName) {
    var helpExample: String? = null
    var isId: Boolean = false
}
