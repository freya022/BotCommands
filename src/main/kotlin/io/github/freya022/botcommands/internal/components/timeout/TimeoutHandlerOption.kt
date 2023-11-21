package io.github.freya022.botcommands.internal.components.timeout

import io.github.freya022.botcommands.internal.core.options.OptionImpl
import io.github.freya022.botcommands.internal.core.options.OptionType
import io.github.freya022.botcommands.internal.parameters.resolvers.TimeoutParameterResolver

internal class TimeoutHandlerOption internal constructor(
    optionBuilder: TimeoutHandlerOptionBuilder,
    val resolver: TimeoutParameterResolver<*, *>
) : OptionImpl(optionBuilder.optionParameter, OptionType.OPTION)