package com.freya02.botcommands.internal.modals

import com.freya02.botcommands.api.core.options.builder.OptionBuilder
import com.freya02.botcommands.api.modals.annotations.ModalInput
import com.freya02.botcommands.api.parameters.ModalParameterResolver
import kotlin.reflect.full.findAnnotation

internal class ModalHandlerInputOption(
    optionBuilder: OptionBuilder,
    val resolver: ModalParameterResolver<*, *>
) : ModalHandlerOption(optionBuilder) {
    val inputName: String = kParameter.findAnnotation<ModalInput>()!!.name
}