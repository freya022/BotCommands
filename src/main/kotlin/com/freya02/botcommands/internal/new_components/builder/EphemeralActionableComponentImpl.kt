package com.freya02.botcommands.internal.new_components.builder

import com.freya02.botcommands.api.components.event.ButtonEvent
import com.freya02.botcommands.api.new_components.builder.IEphemeralActionableComponent
import com.freya02.botcommands.internal.new_components.EphemeralHandler

class EphemeralActionableComponentImpl<T : IEphemeralActionableComponent<T>> : IEphemeralActionableComponent<T> {
    override var handler: EphemeralHandler<*>? = null
        private set

    override fun bindTo(handler: suspend (ButtonEvent) -> Unit): T = this.also { it.handler = EphemeralHandler(handler) } as T
}