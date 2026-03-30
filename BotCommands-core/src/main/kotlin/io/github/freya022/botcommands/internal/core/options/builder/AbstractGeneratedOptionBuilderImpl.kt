package io.github.freya022.botcommands.internal.core.options.builder

import io.github.freya022.botcommands.api.parameters.AggregatedParameter
import io.github.freya022.botcommands.internal.core.options.AbstractGeneratedOption
import io.github.freya022.botcommands.internal.parameters.OptionParameter

abstract class AbstractGeneratedOptionBuilderImpl(
    optionParameter: OptionParameter
) : OptionBuilderImpl(optionParameter) {
    abstract fun toGeneratedOption(parent: AggregatedParameter): AbstractGeneratedOption
}
