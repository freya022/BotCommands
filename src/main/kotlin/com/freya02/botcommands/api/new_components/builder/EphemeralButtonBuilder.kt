package com.freya02.botcommands.api.new_components.builder

interface EphemeralButtonBuilder :
    ButtonBuilder<EphemeralButtonBuilder>,
    IEphemeralActionableComponent<EphemeralButtonBuilder>,
    IEphemeralTimeoutableComponent<EphemeralButtonBuilder>