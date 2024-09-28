package io.github.freya022.botcommands.api.components.builder.button

import io.github.freya022.botcommands.api.components.builder.IPersistentActionableComponent
import io.github.freya022.botcommands.api.components.builder.IPersistentTimeoutableComponent

interface PersistentButtonBuilder : ButtonBuilder<PersistentButtonBuilder>,
    IPersistentActionableComponent<PersistentButtonBuilder>,
    IPersistentTimeoutableComponent<PersistentButtonBuilder>