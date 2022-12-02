package com.freya02.botcommands.api.new_components.builder.button

import com.freya02.botcommands.api.new_components.builder.IEphemeralActionableComponent
import com.freya02.botcommands.api.new_components.builder.IEphemeralTimeoutableComponent

interface EphemeralButtonBuilder :
    ButtonBuilder<EphemeralButtonBuilder>,
    IEphemeralActionableComponent<EphemeralButtonBuilder>,
    IEphemeralTimeoutableComponent<EphemeralButtonBuilder>