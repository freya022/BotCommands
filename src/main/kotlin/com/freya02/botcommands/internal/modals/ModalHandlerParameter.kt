package com.freya02.botcommands.internal.modals

import com.freya02.botcommands.internal.parameters.MethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameterType
import com.freya02.botcommands.internal.utils.ReflectionMetadata.isNullable
import kotlin.reflect.KParameter

abstract class ModalHandlerParameter(
    override val kParameter: KParameter
) : MethodParameter {
    override val methodParameterType = MethodParameterType.OPTION

    override val isOptional: Boolean by lazy { kParameter.isNullable || kParameter.isOptional }
}