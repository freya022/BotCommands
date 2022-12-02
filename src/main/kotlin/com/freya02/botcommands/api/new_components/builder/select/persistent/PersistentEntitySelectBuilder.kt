package com.freya02.botcommands.api.new_components.builder.select.persistent

import com.freya02.botcommands.api.new_components.builder.IPersistentActionableComponent
import com.freya02.botcommands.api.new_components.builder.IPersistentTimeoutableComponent
import com.freya02.botcommands.api.new_components.builder.select.EntitySelectBuilder

interface PersistentEntitySelectBuilder :
    EntitySelectBuilder<PersistentEntitySelectBuilder>,
    IPersistentActionableComponent<PersistentEntitySelectBuilder>,
    IPersistentTimeoutableComponent<PersistentEntitySelectBuilder>