package io.github.freya022.botcommands.api.components.builder

import io.github.freya022.botcommands.internal.components.ComponentDSL
import io.github.freya022.botcommands.internal.components.EphemeralHandler
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

@ComponentDSL
class EphemeralHandlerBuilder<E : GenericComponentInteractionCreateEvent> internal constructor(val handler: suspend (E) -> Unit) {
    @JvmSynthetic
    internal fun build(): EphemeralHandler<E> {
        return EphemeralHandler(handler)
    }
}