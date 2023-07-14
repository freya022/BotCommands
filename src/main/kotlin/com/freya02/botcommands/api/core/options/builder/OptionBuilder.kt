package com.freya02.botcommands.api.core.options.builder

import com.freya02.botcommands.internal.commands.CommandDSL
import com.freya02.botcommands.internal.parameters.OptionParameter

@CommandDSL
abstract class OptionBuilder internal constructor(
    /**
     * Declared name is not unique! (varargs, for example)
     */
    internal val optionParameter: OptionParameter
) {
    internal val owner = optionParameter.typeCheckingFunction

    /**
     * **Note:** Could be an array parameter! In which case this parameter could be repeated on multiple options
     */
    internal val parameter = optionParameter.typeCheckingParameter
}
