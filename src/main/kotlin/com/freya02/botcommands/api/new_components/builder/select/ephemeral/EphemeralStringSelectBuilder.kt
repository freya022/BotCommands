package com.freya02.botcommands.api.new_components.builder.select.ephemeral

import com.freya02.botcommands.api.new_components.builder.IEphemeralActionableComponent
import com.freya02.botcommands.api.new_components.builder.IEphemeralTimeoutableComponent
import com.freya02.botcommands.api.new_components.builder.select.StringSelectBuilder

interface EphemeralStringSelectBuilder :
    StringSelectBuilder<EphemeralStringSelectBuilder>,
    IEphemeralActionableComponent<EphemeralStringSelectBuilder>,
    IEphemeralTimeoutableComponent<EphemeralStringSelectBuilder>