package io.github.freya022.botcommands.api.components.builder

import io.github.freya022.botcommands.internal.components.builder.ConstrainableComponentImpl
import io.github.freya022.botcommands.internal.components.builder.InstanceRetriever
import io.github.freya022.botcommands.internal.components.builder.UniqueComponentImpl

abstract class AbstractComponentBuilder<T : AbstractComponentBuilder<T>> internal constructor(
    private val instanceRetriever: InstanceRetriever<T>
) : BaseComponentBuilder<T>,
    IUniqueComponent<T> by UniqueComponentImpl(instanceRetriever),
    IConstrainableComponent<T> by ConstrainableComponentImpl(instanceRetriever) {

    override val instance: T
        get() = instanceRetriever.instance
}