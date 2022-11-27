package com.freya02.botcommands.internal.new_components.new

import com.freya02.botcommands.internal.data.LifetimeType
import com.freya02.botcommands.internal.new_components.ComponentType

internal class ComponentGroupData internal constructor(
    groupId: Int,
    oneUse: Boolean,
    timeout: ComponentTimeout?,
    internal val componentsIds: List<Int>
): ComponentData(groupId, ComponentType.GROUP, LifetimeType.PERSISTENT, oneUse, null, timeout, null, groupId)