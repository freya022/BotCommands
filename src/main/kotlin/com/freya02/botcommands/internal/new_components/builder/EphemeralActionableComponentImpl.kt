package com.freya02.botcommands.internal.new_components.builder

import com.freya02.botcommands.api.components.builder.IEphemeralActionableComponent
import com.freya02.botcommands.internal.new_components.EphemeralHandler
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

internal class EphemeralActionableComponentImpl<E : GenericComponentInteractionCreateEvent> : IEphemeralActionableComponent<E> {
    override var handler: EphemeralHandler<*>? = null
        private set

    override fun bindTo(handler: suspend (E) -> Unit) {
        this.handler = EphemeralHandler(handler)
    }
}