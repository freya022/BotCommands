package com.freya02.botcommands.internal.modals

import com.freya02.botcommands.api.core.options.builder.OptionBuilder
import com.freya02.botcommands.internal.AbstractOption
import com.freya02.botcommands.internal.parameters.MethodParameterType
import com.freya02.botcommands.internal.utils.ReflectionMetadata.isNullable

abstract class ModalHandlerOption(
    optionBuilder: OptionBuilder
) : AbstractOption {
    override val optionParameter = optionBuilder.optionParameter
    override val methodParameterType = MethodParameterType.OPTION

    override val isOptional: Boolean by lazy { kParameter.isNullable || kParameter.isOptional }
}