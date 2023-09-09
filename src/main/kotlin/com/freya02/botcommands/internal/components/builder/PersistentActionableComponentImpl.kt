package com.freya02.botcommands.internal.components.builder

import com.freya02.botcommands.api.components.builder.IPersistentActionableComponent
import com.freya02.botcommands.internal.components.PersistentHandler

internal class PersistentActionableComponentImpl : IPersistentActionableComponent {
    override var handler: PersistentHandler? = null
        private set

    override fun bindTo(handlerName: String, data: List<Any?>) {
        this.handler = PersistentHandler(handlerName, data)
    }
}