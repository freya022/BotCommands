package com.freya02.botcommands.internal.modals

import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.annotations.ServiceType
import com.freya02.botcommands.api.modals.Modals
import com.freya02.botcommands.api.modals.TextInputBuilder
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle

@BService
@ServiceType([Modals::class])
internal class ModalsImpl(private val modalMaps: ModalMaps) : Modals {
    override fun create(title: String): ModalBuilderImpl {
        return ModalBuilderImpl(modalMaps, title)
    }

    override fun createTextInput(inputName: String, label: String, style: TextInputStyle): TextInputBuilder {
        return TextInputBuilder(modalMaps, inputName, label, style)
    }
}