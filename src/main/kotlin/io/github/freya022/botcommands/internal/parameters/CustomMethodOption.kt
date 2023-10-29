package io.github.freya022.botcommands.internal.parameters

import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import io.github.freya022.botcommands.internal.core.options.OptionImpl
import io.github.freya022.botcommands.internal.core.options.OptionType

class CustomMethodOption(
    optionParameter: OptionParameter,
    val resolver: ICustomResolver<*, *>
) : OptionImpl(optionParameter, OptionType.CUSTOM)