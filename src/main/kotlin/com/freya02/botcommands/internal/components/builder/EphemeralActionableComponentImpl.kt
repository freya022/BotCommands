package com.freya02.botcommands.internal.components.builder

import com.freya02.botcommands.api.ReceiverConsumer
import com.freya02.botcommands.api.components.builder.EphemeralHandlerBuilder
import com.freya02.botcommands.api.components.builder.IEphemeralActionableComponent
import com.freya02.botcommands.internal.components.EphemeralHandler
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

internal class EphemeralActionableComponentImpl<E : GenericComponentInteractionCreateEvent> : IEphemeralActionableComponent<E> {
    override var handler: EphemeralHandler<*>? = null
        private set

    override fun bindTo(handler: suspend (E) -> Unit, block: ReceiverConsumer<EphemeralHandlerBuilder<E>>) {
        this.handler = EphemeralHandlerBuilder(handler).apply(block).build()
    }
}