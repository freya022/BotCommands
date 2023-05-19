package com.freya02.botcommands.api.core.options.builder

import com.freya02.botcommands.internal.parameters.OptionParameter

abstract class OptionBuilder(
    /**
     * Declared name is not unique ! (varargs for example)
     */
    val optionParameter: OptionParameter
) {
    val owner = optionParameter.typeCheckingFunction

    /**
     * **Note:** Could be an array parameter! In which case this parameter could be repeated on multiple options
     */
    @get:JvmSynthetic
    internal val parameter = optionParameter.typeCheckingParameter
}
