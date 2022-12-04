package com.freya02.botcommands.internal.new_components.new

import com.freya02.botcommands.internal.new_components.ComponentType
import com.freya02.botcommands.internal.new_components.LifetimeType

internal class ComponentGroupData internal constructor(
    groupId: Int,
    oneUse: Boolean,
    timeout: ComponentTimeout?,
    internal val componentsIds: List<Int>
): ComponentData(groupId, ComponentType.GROUP, LifetimeType.PERSISTENT, oneUse, null, timeout, null, groupId)