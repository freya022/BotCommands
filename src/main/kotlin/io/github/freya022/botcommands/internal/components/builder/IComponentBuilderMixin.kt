package io.github.freya022.botcommands.internal.components.builder

import io.github.freya022.botcommands.api.components.builder.IComponentBuilder
import io.github.freya022.botcommands.internal.components.ComponentType
import io.github.freya022.botcommands.internal.components.LifetimeType

internal interface IComponentBuilderMixin<T : IComponentBuilder> : IComponentBuilder,
                                                                   BuilderInstanceHolder<T> {

    val componentType: ComponentType
    val lifetimeType: LifetimeType
}