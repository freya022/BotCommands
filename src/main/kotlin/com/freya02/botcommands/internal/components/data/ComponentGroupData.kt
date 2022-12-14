package com.freya02.botcommands.internal.components.new

import com.freya02.botcommands.internal.components.ComponentType
import com.freya02.botcommands.internal.components.LifetimeType
import com.freya02.botcommands.internal.components.data.ComponentTimeout

internal class ComponentGroupData internal constructor(
    groupId: Int,
    oneUse: Boolean,
    timeout: ComponentTimeout?,
    internal val componentsIds: List<Int>
): ComponentData(groupId, ComponentType.GROUP, LifetimeType.PERSISTENT, oneUse, null, timeout, null, groupId)