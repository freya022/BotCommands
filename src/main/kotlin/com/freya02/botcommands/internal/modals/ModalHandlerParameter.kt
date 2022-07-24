package com.freya02.botcommands.internal.modals

import com.freya02.botcommands.internal.findDeclarationName
import com.freya02.botcommands.internal.parameters.MethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameterType
import kotlin.reflect.KParameter

abstract class ModalHandlerParameter(
    override val kParameter: KParameter
) : MethodParameter {
    override val methodParameterType = MethodParameterType.COMMAND
    override val name by lazy { this.kParameter.findDeclarationName() }
}