package com.freya02.botcommands.api.new_components

import com.freya02.botcommands.internal.data.SerializableDataEntity
import com.freya02.botcommands.internal.new_components.ComponentType
import com.freya02.botcommands.internal.new_components.new.ComponentTimeout
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

class ComponentGroup internal constructor(
    internal val oneUse: Boolean,
    internal val timeout: ComponentTimeout?,
    internal val componentsIds: List<String>
): SerializableDataEntity {
    override val type: ComponentType = ComponentType.GROUP

    @JvmSynthetic
    suspend fun await(): GenericComponentInteractionCreateEvent = TODO()
}