package com.freya02.botcommands.api.new_components

import com.freya02.botcommands.internal.data.DataEntityTimeout
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

class ComponentGroup internal constructor(
    internal val oneUse: Boolean,
    internal val timeout: DataEntityTimeout?,
    internal val componentsIds: List<String>
) {
    @JvmSynthetic
    suspend fun await(): GenericComponentInteractionCreateEvent = TODO()
}