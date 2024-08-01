package io.github.freya022.botcommands.internal.modals

import io.github.freya022.botcommands.internal.core.options.OptionImpl
import io.github.freya022.botcommands.internal.core.options.OptionType
import io.github.freya022.botcommands.internal.core.options.builder.OptionBuilderImpl

internal abstract class ModalHandlerOption internal constructor(
    optionBuilder: OptionBuilderImpl
) : OptionImpl(optionBuilder.optionParameter, OptionType.OPTION)