package com.freya02.botcommands.api.commands.application.builder

import com.freya02.botcommands.internal.joinWithQuote
import com.freya02.botcommands.internal.throwUser

abstract class OptionAggregateBuilder(val declaredName: String) {
    internal val optionBuilders: MutableMap<String, OptionBuilder> = mutableMapOf()

    companion object {
        internal inline fun <reified T : OptionAggregateBuilder> Map<String, OptionAggregateBuilder>.findOption(name: String, builderDescription: String): T {
            when (val builder = this[name]) {
                is T -> return builder
                null -> throwUser("Option '$name' was not found in the command declaration, declared options: ${this.keys.joinWithQuote()}")
                else -> throwUser("Option '$name' was found in the command declaration, but $builderDescription was expected (you may have forgotten an annotation, if you are using them)")
            }
        }
    }
}
