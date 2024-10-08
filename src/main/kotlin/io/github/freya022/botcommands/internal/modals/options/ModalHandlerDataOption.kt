package io.github.freya022.botcommands.internal.modals.options

import io.github.freya022.botcommands.internal.core.options.AbstractGeneratedOption
import io.github.freya022.botcommands.internal.modals.options.builder.ModalHandlerDataOptionBuilderImpl

internal class ModalHandlerDataOption internal constructor(
    override val parent: ModalHandlerParameterImpl,
    optionBuilder: ModalHandlerDataOptionBuilderImpl
) : AbstractGeneratedOption(optionBuilder.optionParameter) {

    override val executable get() = parent.executable
}