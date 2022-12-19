package com.freya02.botcommands.internal.modals

import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.core.annotations.ServiceType
import com.freya02.botcommands.api.modals.ModalBuilder
import com.freya02.botcommands.api.modals.Modals
import com.freya02.botcommands.api.modals.TextInputBuilder
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle

@BService
@ServiceType(type = Modals::class)
internal class ModalsImpl(private val modalMaps: ModalMaps) : Modals {
    override fun create(title: String): ModalBuilder {
        return ModalBuilder(modalMaps, title)
    }

    override fun createTextInput(inputName: String, label: String, style: TextInputStyle): TextInputBuilder {
        return TextInputBuilder(modalMaps, inputName, label, style)
    }
}