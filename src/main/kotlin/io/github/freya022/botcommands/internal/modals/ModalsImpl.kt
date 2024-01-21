package io.github.freya022.botcommands.internal.modals

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.modals.Modals
import io.github.freya022.botcommands.api.modals.TextInputBuilder
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle

@BService
internal class ModalsImpl(private val modalMaps: ModalMaps) : Modals {
    override fun create(title: String): ModalBuilderImpl {
        return ModalBuilderImpl(modalMaps, title)
    }

    override fun createTextInput(inputName: String, label: String, style: TextInputStyle): TextInputBuilder {
        return TextInputBuilderImpl(modalMaps, inputName, label, style)
    }
}