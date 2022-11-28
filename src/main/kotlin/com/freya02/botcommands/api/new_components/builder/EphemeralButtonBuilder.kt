package com.freya02.botcommands.api.new_components.builder

import com.freya02.botcommands.api.components.event.ButtonEvent

interface EphemeralButtonBuilder : ButtonBuilder<EphemeralButtonBuilder>, IEphemeralTimeoutableComponent<EphemeralButtonBuilder> {
    //TODO suspend & java
    fun bindTo(handler: (ButtonEvent) -> Unit): EphemeralButtonBuilder
}