package io.github.freya022.botcommands.api.components.builder.group

import io.github.freya022.botcommands.api.components.builder.IEphemeralTimeoutableComponent

interface EphemeralComponentGroupBuilder :
        ComponentGroupBuilder<EphemeralComponentGroupBuilder>,
        IEphemeralTimeoutableComponent<EphemeralComponentGroupBuilder>