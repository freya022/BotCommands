package com.freya02.botcommands.api.new_components.builder

interface ComponentGroupBuilder :
    IPersistentTimeoutableComponent<ComponentGroupBuilder>,
    IEphemeralTimeoutableComponent<ComponentGroupBuilder>,
    IUniqueComponent<ComponentGroupBuilder> {
    val componentIds: List<String>
}