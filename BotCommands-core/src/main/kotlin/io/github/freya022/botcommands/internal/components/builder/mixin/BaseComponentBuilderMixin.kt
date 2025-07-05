package io.github.freya022.botcommands.internal.components.builder.mixin

import io.github.freya022.botcommands.api.components.builder.BaseComponentBuilder

internal interface BaseComponentBuilderMixin<T : BaseComponentBuilder<T>> :
        IComponentBuilderMixin<T>,
        ITimeoutableComponentMixin<T>,
        IActionableComponentMixin<T>,
        IConstrainableComponentMixin<T>,
        IUniqueComponentMixin<T>