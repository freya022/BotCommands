package com.freya02.botcommands.api.commands

import com.freya02.botcommands.api.core.options.builder.OptionBuilder
import com.freya02.botcommands.internal.parameters.OptionParameter

abstract class CommandOptionBuilder(
    optionParameter: OptionParameter,
    val optionName: String
) : OptionBuilder(optionParameter) {
    /**
     * Switch managed by the vararg aggregates
     */
    @get:JvmSynthetic @set:JvmSynthetic
    internal var isOptional = false
}
