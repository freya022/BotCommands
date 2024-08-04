package io.github.freya022.botcommands.internal.core.options.builder

import io.github.freya022.botcommands.api.core.options.builder.OptionBuilder
import io.github.freya022.botcommands.internal.parameters.OptionParameter

internal abstract class OptionBuilderImpl internal constructor(
    /**
     * Declared name is not unique! (varargs, for example)
     */
    internal val optionParameter: OptionParameter
) : OptionBuilder {
    internal val owner = optionParameter.typeCheckingFunction

    /**
     * **Note:** Could be an array parameter! In which case this parameter could be repeated on multiple options
     */
    internal val parameter = optionParameter.typeCheckingParameter
}