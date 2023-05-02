package com.freya02.botcommands.internal.parameters

import com.freya02.botcommands.api.parameters.ICustomResolver
import com.freya02.botcommands.internal.core.options.OptionImpl
import com.freya02.botcommands.internal.core.options.OptionType

class CustomMethodOption(
    optionParameter: OptionParameter,
    val resolver: ICustomResolver<*, *>
) : OptionImpl(optionParameter, OptionType.CUSTOM)