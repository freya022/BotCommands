package io.github.freya022.botcommands.api.commands

import io.github.freya022.botcommands.api.core.options.builder.OptionBuilder
import io.github.freya022.botcommands.internal.core.options.OptionImpl
import io.github.freya022.botcommands.internal.parameters.OptionParameter

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
