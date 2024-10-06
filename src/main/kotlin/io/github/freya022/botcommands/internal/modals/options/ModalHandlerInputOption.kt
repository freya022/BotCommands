package io.github.freya022.botcommands.internal.modals.options

import io.github.freya022.botcommands.api.core.utils.findAnnotationRecursive
import io.github.freya022.botcommands.api.modals.annotations.ModalInput
import io.github.freya022.botcommands.api.parameters.resolvers.ModalParameterResolver
import io.github.freya022.botcommands.internal.modals.ModalHandlerInfo
import io.github.freya022.botcommands.internal.modals.options.builder.ModalHandlerInputOptionBuilderImpl

internal class ModalHandlerInputOption(
    override val executable: ModalHandlerInfo,
    optionBuilder: ModalHandlerInputOptionBuilderImpl,
    val resolver: ModalParameterResolver<*, *>
) : ModalHandlerOption(optionBuilder) {
    val inputName: String = kParameter.findAnnotationRecursive<ModalInput>()!!.name
}