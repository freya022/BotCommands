package io.github.freya022.botcommands.internal.modals

import io.github.freya022.botcommands.api.modals.TextInputBuilder
import net.dv8tion.jda.api.components.textinput.TextInput
import net.dv8tion.jda.api.components.textinput.TextInputStyle

internal class TextInputBuilderImpl internal constructor(
    private val modalMaps: ModalMaps,
    private val inputName: String,
    style: TextInputStyle
) : TextInputBuilder(style) {
    override fun build(): TextInput {
        internetSetId(modalMaps.insertInput(InputData(inputName)))

        return jdaBuild()
    }
}
