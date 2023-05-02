package com.freya02.botcommands.internal.parameters

import com.freya02.botcommands.api.parameters.ICustomResolver
import com.freya02.botcommands.internal.core.options.AbstractOptionImpl
import com.freya02.botcommands.internal.core.options.OptionType

class CustomMethodOption(
    optionParameter: OptionParameter,
    val resolver: ICustomResolver<*, *>
) : AbstractOptionImpl(optionParameter, OptionType.CUSTOM)