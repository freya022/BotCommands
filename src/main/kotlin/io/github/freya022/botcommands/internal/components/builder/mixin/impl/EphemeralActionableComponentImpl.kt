package io.github.freya022.botcommands.internal.components.builder.mixin.impl

import io.github.freya022.botcommands.api.components.builder.IEphemeralActionableComponent
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.components.builder.AbstractActionableComponent
import io.github.freya022.botcommands.internal.components.builder.InstanceRetriever
import io.github.freya022.botcommands.internal.components.builder.mixin.IEphemeralActionableComponentMixin
import io.github.freya022.botcommands.internal.components.handler.EphemeralHandler
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

internal class EphemeralActionableComponentImpl<T : IEphemeralActionableComponent<T, E>, E : GenericComponentInteractionCreateEvent> internal constructor(
    context: BContext,
    instanceRetriever: InstanceRetriever<T>
) : AbstractActionableComponent<T>(context, instanceRetriever),
    IEphemeralActionableComponentMixin<T, E> {

    override var handler: EphemeralHandler<*>? = null
        private set

    override fun bindTo(handler: suspend (E) -> Unit): T = applyInstance {
        this.handler = EphemeralHandler(handler)
    }
}