package io.github.freya022.botcommands.internal.modals.options

import io.github.freya022.botcommands.api.modals.annotations.ModalInput
import io.github.freya022.botcommands.api.parameters.resolvers.ModalParameterResolver
import io.github.freya022.botcommands.internal.modals.options.builder.ModalHandlerInputOptionBuilderImpl
import kotlin.reflect.full.findAnnotation

internal class ModalHandlerInputOption(
    optionBuilder: ModalHandlerInputOptionBuilderImpl,
    val resolver: ModalParameterResolver<*, *>
) : ModalHandlerOption(optionBuilder) {
    val inputName: String = kParameter.findAnnotation<ModalInput>()!!.name
}