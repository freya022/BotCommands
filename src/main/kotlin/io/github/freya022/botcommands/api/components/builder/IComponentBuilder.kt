package io.github.freya022.botcommands.api.components.builder

import io.github.freya022.botcommands.internal.components.ComponentDSL
import io.github.freya022.botcommands.internal.components.ComponentType
import io.github.freya022.botcommands.internal.components.LifetimeType

@ComponentDSL
interface IComponentBuilder {
    val componentType: ComponentType
    val lifetimeType: LifetimeType
}