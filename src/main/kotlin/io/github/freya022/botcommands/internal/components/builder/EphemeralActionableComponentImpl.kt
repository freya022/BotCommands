package io.github.freya022.botcommands.internal.components.builder

import io.github.freya022.botcommands.api.ReceiverConsumer
import io.github.freya022.botcommands.api.components.builder.EphemeralHandlerBuilder
import io.github.freya022.botcommands.api.components.builder.IEphemeralActionableComponent
import io.github.freya022.botcommands.internal.components.EphemeralHandler
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

internal class EphemeralActionableComponentImpl<E : GenericComponentInteractionCreateEvent> : AbstractActionableComponent(), IEphemeralActionableComponent<E> {
    override var handler: EphemeralHandler<*>? = null
        private set

    override fun bindTo(handler: suspend (E) -> Unit, block: ReceiverConsumer<EphemeralHandlerBuilder<E>>) {
        this.handler = EphemeralHandlerBuilder(handler).apply(block).build()
    }
}