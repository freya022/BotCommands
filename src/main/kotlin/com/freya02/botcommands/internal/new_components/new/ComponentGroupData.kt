package com.freya02.botcommands.internal.new_components.new

import com.freya02.botcommands.internal.data.LifetimeType
import com.freya02.botcommands.internal.new_components.ComponentType

internal class ComponentGroupData internal constructor(
    componentId: Int,
    oneUse: Boolean,
    timeout: ComponentTimeout?,
    groupId: Int,
    internal val componentsIds: List<Int>
): ComponentData(componentId, ComponentType.GROUP, LifetimeType.PERSISTENT, oneUse, null, timeout, null, groupId)