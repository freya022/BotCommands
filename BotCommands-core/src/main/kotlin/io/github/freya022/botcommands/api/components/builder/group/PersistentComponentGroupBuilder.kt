package io.github.freya022.botcommands.api.components.builder.group

import io.github.freya022.botcommands.api.components.builder.IPersistentTimeoutableComponent

interface PersistentComponentGroupBuilder :
        ComponentGroupBuilder<PersistentComponentGroupBuilder>,
        IPersistentTimeoutableComponent<PersistentComponentGroupBuilder>