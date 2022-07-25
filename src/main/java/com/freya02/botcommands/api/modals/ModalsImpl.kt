package com.freya02.botcommands.api.modals

import com.freya02.botcommands.core.api.annotations.BService
import com.freya02.botcommands.core.internal.ServiceContainer
import com.freya02.botcommands.internal.BContextImpl
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle

@BService
internal class ModalsImpl(private val context: BContextImpl, serviceContainer: ServiceContainer) : Modals {
    init {
        serviceContainer.putServiceAs<Modals>(this)
    }

    override fun create(title: String, handlerName: String, vararg userData: Any?): ModalBuilder {
        return ModalBuilder(context.modalMaps, title, handlerName, userData)
    }

    override fun createTextInput(inputName: String, label: String, style: TextInputStyle): TextInputBuilder {
        return TextInputBuilder(context.modalMaps, inputName, label, style)
    }
}