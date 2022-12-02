package com.freya02.botcommands.api.new_components.builder

import com.freya02.botcommands.api.components.event.ButtonEvent
import java.util.function.Consumer

interface IEphemeralActionableComponent<T : IEphemeralActionableComponent<T>> : IActionableComponent {
    fun bindTo(handler: Consumer<ButtonEvent>): T = bindTo { handler.accept(it) }

    @JvmSynthetic
    fun bindTo(handler: suspend (ButtonEvent) -> Unit): T
}