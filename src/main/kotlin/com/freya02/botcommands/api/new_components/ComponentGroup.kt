package com.freya02.botcommands.api.new_components

import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

class ComponentGroup(private val componentIds: List<String>) {
    @JvmSynthetic
    suspend fun await(): GenericComponentInteractionCreateEvent = TODO()
}