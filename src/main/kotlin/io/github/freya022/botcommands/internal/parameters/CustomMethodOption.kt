package io.github.freya022.botcommands.internal.parameters

import io.github.freya022.botcommands.api.parameters.AggregatedParameter
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import io.github.freya022.botcommands.internal.core.options.OptionImpl
import io.github.freya022.botcommands.internal.core.options.OptionType

internal class CustomMethodOption internal constructor(
    override val parent: AggregatedParameter,
    optionParameter: OptionParameter,
    val resolver: ICustomResolver<*, *>
) : OptionImpl(optionParameter, OptionType.CUSTOM) {

    override val executable get() = parent.executable
}