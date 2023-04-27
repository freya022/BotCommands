package com.freya02.botcommands.api.core.options.builder

import com.freya02.botcommands.internal.findDeclarationName
import com.freya02.botcommands.internal.joinWithQuote
import com.freya02.botcommands.internal.throwUser
import kotlin.reflect.KFunction
import kotlin.reflect.full.valueParameters

abstract class OptionBuilder(
    val owner: KFunction<*>,
    /**
     * Is not unique ! (varargs for example)
     */
    val declaredName: String
) {
    /**
     * **Note:** Could be an array parameter! In which case this parameter could be repeated on multiple options
     */
    internal val parameter = owner.valueParameters.first { it.findDeclarationName() == declaredName }
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
