package com.freya02.botcommands.internal.new_components.builder

import com.freya02.botcommands.api.new_components.builder.IPersistentActionableComponent
import com.freya02.botcommands.internal.new_components.PersistentHandler

class PersistentActionableComponentImpl : IPersistentActionableComponent<PersistentActionableComponentImpl> {
    override var handler: PersistentHandler? = null
        private set

    override fun bindTo(handlerName: String, vararg data: Any?): PersistentActionableComponentImpl =
        this.also { handler = PersistentHandler(handlerName, data) }
}