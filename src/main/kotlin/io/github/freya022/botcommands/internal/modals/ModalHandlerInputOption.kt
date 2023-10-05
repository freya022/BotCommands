package io.github.freya022.botcommands.internal.modals

import io.github.freya022.botcommands.api.core.options.builder.OptionBuilder
import io.github.freya022.botcommands.api.modals.annotations.ModalInput
import io.github.freya022.botcommands.api.parameters.ModalParameterResolver
import kotlin.reflect.full.findAnnotation

internal class ModalHandlerInputOption(
    optionBuilder: OptionBuilder,
    val resolver: ModalParameterResolver<*, *>
) : ModalHandlerOption(optionBuilder) {
    val inputName: String = kParameter.findAnnotation<ModalInput>()!!.name
}