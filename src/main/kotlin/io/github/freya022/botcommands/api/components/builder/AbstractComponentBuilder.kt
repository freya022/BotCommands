package io.github.freya022.botcommands.api.components.builder

import io.github.freya022.botcommands.internal.components.builder.ConstrainableComponentImpl
import io.github.freya022.botcommands.internal.components.builder.UniqueComponentImpl

abstract class AbstractComponentBuilder<T : AbstractComponentBuilder<T>> internal constructor() :
    BaseComponentBuilder<T>,
    IUniqueComponent<T> by UniqueComponentImpl(),
    IConstrainableComponent<T> by ConstrainableComponentImpl() {

    override val instance: T
        get() = super<BaseComponentBuilder>.instance
}