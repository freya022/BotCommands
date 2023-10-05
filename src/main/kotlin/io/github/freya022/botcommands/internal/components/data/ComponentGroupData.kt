package io.github.freya022.botcommands.internal.components.data

import io.github.freya022.botcommands.api.components.data.ComponentTimeout
import io.github.freya022.botcommands.internal.components.ComponentType
import io.github.freya022.botcommands.internal.components.LifetimeType

internal class ComponentGroupData internal constructor(
    groupId: Int,
    oneUse: Boolean,
    rateLimitGroup: String?,
    timeout: ComponentTimeout?,
    internal val componentsIds: List<Int>
): ComponentData(groupId, ComponentType.GROUP, LifetimeType.PERSISTENT, oneUse, rateLimitGroup, null, timeout, null, groupId)