package com.freya02.botcommands.internal.modals

import com.freya02.botcommands.annotations.api.modals.annotations.ModalData
import com.freya02.botcommands.annotations.api.modals.annotations.ModalInput
import com.freya02.botcommands.api.parameters.ModalParameterResolver
import com.freya02.botcommands.internal.application.CommandParameter
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

class ModalHandlerParameter(
    parameter: KParameter,
    index: Int
) : CommandParameter<ModalParameterResolver>(
    ModalParameterResolver::class, parameter, index
) {
    val isModalData: Boolean
    val isModalInput: Boolean
    var modalInputName: String? = null

    init {
        val modalInput = parameter.findAnnotation<ModalInput>()

        isModalData = parameter.hasAnnotation<ModalData>()
        isModalInput = modalInput != null

        require(isModalData xor isModalInput) {
            "Parameter #$index cannot be both modal data and modal input"
        }

        modalInputName = when {
            modalInput != null -> modalInput.name
            else -> null
        }
    }

    override val optionAnnotations
        get() = listOf(ModalData::class, ModalInput::class)
    override val resolvableAnnotations
        get() = listOf(ModalInput::class)
}