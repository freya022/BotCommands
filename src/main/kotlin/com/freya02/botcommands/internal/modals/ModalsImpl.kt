package com.freya02.botcommands.internal.modals

import com.freya02.botcommands.api.modals.ModalBuilder
import com.freya02.botcommands.api.modals.Modals
import com.freya02.botcommands.api.modals.TextInputBuilder
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle

internal class ModalsImpl(private val modalMaps: ModalMaps) : Modals {
    override fun create(title: String, handlerName: String, vararg userData: Any?): ModalBuilder {
        return ModalBuilder(modalMaps, title, handlerName, userData)
    }

    override fun createTextInput(inputName: String, label: String, style: TextInputStyle): TextInputBuilder {
        return TextInputBuilder(modalMaps, inputName, label, style)
    }
}