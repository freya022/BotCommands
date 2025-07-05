package io.github.freya022.botcommands.api.components.builder

import io.github.freya022.botcommands.api.core.BContext

interface BaseComponentBuilder<T : BaseComponentBuilder<T>> :
        IComponentBuilder,
        ITimeoutableComponent<T>,
        IActionableComponent<T>,
        IConstrainableComponent<T>,
        IUniqueComponent<T> {

    override val context: BContext
}