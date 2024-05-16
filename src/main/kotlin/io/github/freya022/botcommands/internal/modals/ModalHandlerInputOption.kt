package io.github.freya022.botcommands.internal.modals

import io.github.freya022.botcommands.api.modals.annotations.ModalInput
import io.github.freya022.botcommands.api.parameters.resolvers.ModalParameterResolver
import kotlin.reflect.full.findAnnotation

internal class ModalHandlerInputOption(
    optionBuilder: ModalHandlerInputOptionBuilder,
    val resolver: ModalParameterResolver<*, *>
) : ModalHandlerOption(optionBuilder) {
    val inputName: String = kParameter.findAnnotation<ModalInput>()!!.name
}