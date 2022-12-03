package com.freya02.botcommands.api.new_components.builder

import com.freya02.botcommands.internal.new_components.ComponentHandler
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import java.util.function.Consumer

interface IActionableComponent {
    val handler: ComponentHandler?
}

interface IPersistentActionableComponent : IActionableComponent {
    fun bindTo(handlerName: String, vararg data: Any?)
}

interface IEphemeralActionableComponent<E : GenericComponentInteractionCreateEvent> : IActionableComponent {
    //TODO (docs) warn about captured jda entities
    fun bindTo(handler: Consumer<E>) = bindTo { handler.accept(it) }

    @JvmSynthetic
    fun bindTo(handler: suspend (E) -> Unit)
}
