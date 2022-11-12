package com.freya02.botcommands.internal.new_components

import com.freya02.botcommands.api.components.InteractionConstraints
import com.freya02.botcommands.api.new_components.ComponentTimeoutInfo
import com.freya02.botcommands.internal.data.SerializableDataEntity

internal class PersistentComponentData(
    override val type: ComponentType,
    internal val oneUse: Boolean,
    internal val constraints: InteractionConstraints,
    internal val timeoutInfo: ComponentTimeoutInfo?,
    internal val persistentHandler: PersistentHandler
) : SerializableDataEntity