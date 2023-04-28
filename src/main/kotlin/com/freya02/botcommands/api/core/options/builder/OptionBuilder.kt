package com.freya02.botcommands.api.core.options.builder

import com.freya02.botcommands.internal.joinWithQuote
import com.freya02.botcommands.internal.parameters.MultiParameter
import com.freya02.botcommands.internal.throwUser

abstract class OptionBuilder(
    /**
     * Declared name is not unique ! (varargs for example)
     */
    val multiParameter: MultiParameter
) {
    val owner = multiParameter.typeCheckingFunction

    /**
     * **Note:** Could be an array parameter! In which case this parameter could be repeated on multiple options
     */
    internal val parameter = multiParameter.typeCheckingParameter
    @Deprecated("Use 'parameter' instead, beware of array types")
    internal val type = parameter.type

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
