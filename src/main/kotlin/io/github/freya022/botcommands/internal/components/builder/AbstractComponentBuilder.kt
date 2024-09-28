package io.github.freya022.botcommands.internal.components.builder

import io.github.freya022.botcommands.api.components.builder.BaseComponentBuilder
import io.github.freya022.botcommands.api.components.builder.IConstrainableComponent
import io.github.freya022.botcommands.api.components.builder.IUniqueComponent

internal abstract class AbstractComponentBuilder<T : BaseComponentBuilder<T>> internal constructor(
    private val instanceRetriever: InstanceRetriever<T>
) : BaseComponentBuilderMixin<T>,
    IUniqueComponent<T> by UniqueComponentImpl(instanceRetriever),
    IConstrainableComponent<T> by ConstrainableComponentImpl(instanceRetriever) {

    override val instance: T
        get() = instanceRetriever.instance
}