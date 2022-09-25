package com.freya02.botcommands.api.commands.application.builder

import com.freya02.botcommands.internal.joinWithQuote
import com.freya02.botcommands.internal.throwUser

abstract class OptionBuilder(val declaredName: String, val optionName: String) {
    companion object {
        internal inline fun <reified T : OptionBuilder> Map<String, OptionBuilder>.findOption(name: String, builderDescription: String): T {
            when (val builder = this[name]) {
                is T -> return builder
                null -> throwUser("Option '$name' was not found in the command declaration, declared options: ${this.keys.joinWithQuote()}")
                else -> throwUser("Option '$name' was found in the command declaration, but $builderDescription was expected (you may have forgotten an annotation, if you are using them)")
            }
        }
    }
}
