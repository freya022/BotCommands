package com.freya02.botcommands.internal.components.data

import com.freya02.botcommands.api.components.data.ComponentTimeout
import com.freya02.botcommands.internal.components.ComponentType
import com.freya02.botcommands.internal.components.LifetimeType

internal class ComponentGroupData internal constructor(
    groupId: Int,
    oneUse: Boolean,
    timeout: ComponentTimeout?,
    internal val componentsIds: List<Int>
): ComponentData(groupId, ComponentType.GROUP, LifetimeType.PERSISTENT, oneUse, null, timeout, null) {
    val groupId: Int
        get() = componentId
}