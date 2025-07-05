package io.github.freya022.botcommands.api.components.builder.button

import io.github.freya022.botcommands.api.components.Button
import io.github.freya022.botcommands.api.components.builder.BaseComponentBuilder
import javax.annotation.CheckReturnValue

interface ButtonBuilder<T : ButtonBuilder<T>> : BaseComponentBuilder<T> {
    @CheckReturnValue
    fun build(): Button
}