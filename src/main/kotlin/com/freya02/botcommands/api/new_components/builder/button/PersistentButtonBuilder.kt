package com.freya02.botcommands.api.new_components.builder.button

import com.freya02.botcommands.api.new_components.builder.IPersistentActionableComponent
import com.freya02.botcommands.api.new_components.builder.IPersistentTimeoutableComponent

interface PersistentButtonBuilder :
    ButtonBuilder<PersistentButtonBuilder>,
    IPersistentActionableComponent<PersistentButtonBuilder>,
    IPersistentTimeoutableComponent<PersistentButtonBuilder>