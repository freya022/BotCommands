package com.freya02.botcommands.internal.new_components.builder

import com.freya02.botcommands.api.new_components.builder.IPersistentActionableComponent
import com.freya02.botcommands.internal.new_components.PersistentHandler

internal class PersistentActionableComponentImpl<T : IPersistentActionableComponent<T>> : IPersistentActionableComponent<T> {
    override var handler: PersistentHandler? = null
        private set

    override fun bindTo(handlerName: String, vararg data: Any?): T =
        this.also { handler = PersistentHandler(handlerName, data) } as T
}