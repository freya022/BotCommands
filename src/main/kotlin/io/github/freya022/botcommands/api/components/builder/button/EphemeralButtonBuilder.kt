package io.github.freya022.botcommands.api.components.builder.button

import io.github.freya022.botcommands.api.components.builder.IEphemeralActionableComponent
import io.github.freya022.botcommands.api.components.builder.IEphemeralTimeoutableComponent
import io.github.freya022.botcommands.api.components.event.ButtonEvent

interface EphemeralButtonBuilder : ButtonBuilder<EphemeralButtonBuilder>,
    IEphemeralActionableComponent<EphemeralButtonBuilder, ButtonEvent>,
    IEphemeralTimeoutableComponent<EphemeralButtonBuilder>