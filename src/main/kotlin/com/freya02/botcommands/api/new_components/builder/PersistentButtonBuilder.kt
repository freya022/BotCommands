package com.freya02.botcommands.api.new_components.builder

interface PersistentButtonBuilder :
    ButtonBuilder<PersistentButtonBuilder>,
    IPersistentActionableComponent<PersistentButtonBuilder>,
    IPersistentTimeoutableComponent<PersistentButtonBuilder>