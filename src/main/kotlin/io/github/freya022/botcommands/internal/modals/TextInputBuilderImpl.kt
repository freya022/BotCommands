package io.github.freya022.botcommands.internal.modals

import io.github.freya022.botcommands.api.modals.TextInputBuilder
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle

internal class TextInputBuilderImpl internal constructor(
    private val modalMaps: ModalMaps,
    private val inputName: String,
    label: String?,
    style: TextInputStyle?
) : TextInputBuilder(label, style) {
    override fun build(): TextInput {
        id = modalMaps.insertInput(InputData(inputName), id)

        return jdaBuild()
    }
}
