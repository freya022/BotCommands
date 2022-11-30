package com.freya02.botcommands.api.new_components.builder

import com.freya02.botcommands.api.components.event.ButtonEvent

interface IEphemeralActionableComponent<T : IEphemeralActionableComponent<T>> {
    //TODO suspend & java
    fun bindTo(handler: (ButtonEvent) -> Unit): EphemeralButtonBuilder
}