package com.freya02.botcommands.api.components.builder

import com.freya02.botcommands.internal.components.ComponentDSL
import com.freya02.botcommands.internal.components.EphemeralHandler
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

@ComponentDSL
class EphemeralHandlerBuilder<E : GenericComponentInteractionCreateEvent> internal constructor(val handler: suspend (E) -> Unit) {
    @JvmSynthetic
    internal fun build(): EphemeralHandler<E> {
        return EphemeralHandler(handler)
    }
}