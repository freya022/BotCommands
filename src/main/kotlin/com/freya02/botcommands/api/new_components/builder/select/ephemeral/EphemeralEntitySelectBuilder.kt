package com.freya02.botcommands.api.new_components.builder.select.ephemeral

import com.freya02.botcommands.api.new_components.builder.IEphemeralActionableComponent
import com.freya02.botcommands.api.new_components.builder.IEphemeralTimeoutableComponent
import com.freya02.botcommands.api.new_components.builder.select.EntitySelectBuilder

interface EphemeralEntitySelectBuilder :
    EntitySelectBuilder<EphemeralEntitySelectBuilder>,
    IEphemeralActionableComponent<EphemeralEntitySelectBuilder>,
    IEphemeralTimeoutableComponent<EphemeralEntitySelectBuilder>