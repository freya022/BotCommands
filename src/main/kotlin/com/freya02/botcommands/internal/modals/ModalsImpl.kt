package com.freya02.botcommands.internal.modals

import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.modals.ModalBuilder
import com.freya02.botcommands.api.modals.Modals
import com.freya02.botcommands.api.modals.TextInputBuilder
import com.freya02.botcommands.internal.core.ServiceContainer
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle

@BService
internal class ModalsImpl(serviceContainer: ServiceContainer, private val modalMaps: ModalMaps) : Modals {
    init {
        serviceContainer.putServiceAs<Modals>(this)
    }

    override fun create(title: String, handlerName: String, vararg userData: Any?): ModalBuilder {
        return ModalBuilder(modalMaps, title, handlerName, userData)
    }

    override fun createTextInput(inputName: String, label: String, style: TextInputStyle): TextInputBuilder {
        return TextInputBuilder(modalMaps, inputName, label, style)
    }
}