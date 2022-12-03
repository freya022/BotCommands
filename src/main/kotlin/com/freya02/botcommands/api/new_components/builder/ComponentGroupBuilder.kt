package com.freya02.botcommands.api.new_components.builder

interface ComponentGroupBuilder :
    IPersistentTimeoutableComponent,
    IEphemeralTimeoutableComponent,
    IUniqueComponent {
    val componentIds: List<String>
}