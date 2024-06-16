package io.github.freya022.botcommands.internal.core.options

import io.github.freya022.botcommands.internal.parameters.OptionParameter

internal abstract class AbstractGeneratedOption(
    optionParameter: OptionParameter
) : OptionImpl(optionParameter, OptionType.GENERATED)