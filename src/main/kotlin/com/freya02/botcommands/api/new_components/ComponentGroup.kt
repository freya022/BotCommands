package com.freya02.botcommands.api.new_components

import com.freya02.botcommands.internal.data.DataEntityTimeout
import com.freya02.botcommands.internal.data.SerializableDataEntity
import com.freya02.botcommands.internal.new_components.ComponentType
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

class ComponentGroup internal constructor(
    internal val oneUse: Boolean,
    internal val timeout: DataEntityTimeout?,
    internal val componentsIds: List<String>
): SerializableDataEntity {
    override val type: ComponentType = ComponentType.GROUP

    @JvmSynthetic
    suspend fun await(): GenericComponentInteractionCreateEvent = TODO()
}