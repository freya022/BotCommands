package com.freya02.botcommands.api.commands

import com.freya02.botcommands.api.core.options.builder.OptionBuilder
import com.freya02.botcommands.internal.core.options.OptionImpl
import com.freya02.botcommands.internal.parameters.OptionParameter

abstract class CommandOptionBuilder internal constructor(
    optionParameter: OptionParameter
) : OptionBuilder(optionParameter) {
    /**
     * Switch managed by the vararg aggregates
     *
     * @see OptionImpl.isOptionalOrNullable
     */
    internal var isOptional: Boolean? = null
}
