package com.freya02.botcommands.internal.new_components.builder

import com.freya02.botcommands.api.components.event.ButtonEvent
import com.freya02.botcommands.api.new_components.builder.IEphemeralActionableComponent
import com.freya02.botcommands.internal.new_components.EphemeralHandler

class EphemeralActionableComponentImpl : IEphemeralActionableComponent<EphemeralActionableComponentImpl> {
    override var handler: EphemeralHandler<*>? = null
        private set

    override fun bindTo(handler: suspend (ButtonEvent) -> Unit): EphemeralActionableComponentImpl = this.also { it.handler = EphemeralHandler(handler) }
}