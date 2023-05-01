package com.freya02.botcommands.internal.modals

import com.freya02.botcommands.internal.commands.GeneratedMethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameterType
import com.freya02.botcommands.internal.utils.ReflectionMetadata.isNullable

class ModalHandlerDataOption internal constructor(
    optionBuilder: ModalHandlerDataOptionBuilder
) : GeneratedMethodParameter {
    override val methodParameterType = MethodParameterType.GENERATED
    override val optionParameter = optionBuilder.optionParameter

    override val isOptional: Boolean by lazy { kParameter.isNullable || kParameter.isOptional }
}