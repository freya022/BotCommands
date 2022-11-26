package com.freya02.botcommands.api.new_components.builder

import com.freya02.botcommands.api.components.InteractionConstraints
import com.freya02.botcommands.internal.data.LifetimeType
import com.freya02.botcommands.internal.new_components.ComponentHandler
import com.freya02.botcommands.internal.new_components.ComponentType
import com.freya02.botcommands.internal.new_components.new.ComponentTimeout

interface ComponentBuilder {
    val componentType: ComponentType
    val lifetimeType: LifetimeType
    val oneUse: Boolean
    val constraints: InteractionConstraints
    val timeout: ComponentTimeout?
    val handler: ComponentHandler?
}