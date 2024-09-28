package io.github.freya022.botcommands.internal.components.builder

import io.github.freya022.botcommands.api.components.builder.IActionableComponent
import io.github.freya022.botcommands.api.components.builder.IEphemeralActionableComponent
import io.github.freya022.botcommands.api.components.builder.IPersistentActionableComponent
import io.github.freya022.botcommands.api.components.ratelimit.ComponentRateLimitReference
import io.github.freya022.botcommands.internal.components.handler.ComponentHandler
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

internal interface IActionableComponentMixin<T : IActionableComponent<T>> : IActionableComponent<T>,
                                                                            BuilderInstanceHolder<T> {

    val handler: ComponentHandler?

    val rateLimitReference: ComponentRateLimitReference?
}

internal interface IPersistentActionableComponentMixin<T : IPersistentActionableComponent<T>> :
        IPersistentActionableComponent<T>,
        IActionableComponentMixin<T>

internal interface IEphemeralActionableComponentMixin<T : IEphemeralActionableComponent<T, E>, E : GenericComponentInteractionCreateEvent> :
        IEphemeralActionableComponent<T, E>,
        IActionableComponentMixin<T>
