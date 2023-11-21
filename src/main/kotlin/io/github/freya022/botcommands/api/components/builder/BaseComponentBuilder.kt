package io.github.freya022.botcommands.api.components.builder

interface BaseComponentBuilder<T : BaseComponentBuilder<T>> :
    IComponentBuilder,
    BuilderInstanceHolder<T>,
    ITimeoutableComponent<T>,
    IActionableComponent<T>,
    IConstrainableComponent<T>,
    IUniqueComponent<T>