package com.freya02.botcommands.api.new_components.builder

import com.freya02.botcommands.api.components.event.ButtonEvent
import com.freya02.botcommands.internal.new_components.ComponentHandler
import java.util.function.Consumer

interface IActionableComponent {
    val handler: ComponentHandler?
}

interface IPersistentActionableComponent<T : IPersistentActionableComponent<T>> : IActionableComponent {
    fun bindTo(handlerName: String, vararg data: Any?): T
}

interface IEphemeralActionableComponent<T : IEphemeralActionableComponent<T>> : IActionableComponent {
    fun bindTo(handler: Consumer<ButtonEvent>): T = bindTo { handler.accept(it) }

    @JvmSynthetic
    fun bindTo(handler: suspend (ButtonEvent) -> Unit): T
}
