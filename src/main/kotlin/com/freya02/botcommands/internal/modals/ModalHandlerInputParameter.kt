package com.freya02.botcommands.internal.modals

import com.freya02.botcommands.annotations.api.modals.annotations.ModalInput
import com.freya02.botcommands.api.parameters.ModalParameterResolver
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation

class ModalHandlerInputParameter(
    kParameter: KParameter,
    val resolver: ModalParameterResolver
) : ModalHandlerParameter(kParameter) {
    val inputName: String

    init {
        inputName = kParameter.findAnnotation<ModalInput>()!!.name
    }
}