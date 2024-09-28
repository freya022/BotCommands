package io.github.freya022.botcommands.internal.components.builder

import io.github.freya022.botcommands.api.components.builder.IUniqueComponent

internal class UniqueComponentImpl<T : IUniqueComponent<T>> internal constructor(
    override val instanceRetriever: InstanceRetriever<T>
) : BuilderInstanceHolderImpl<T>(),
    IUniqueComponentMixin<T> {

    override var singleUse: Boolean = false

    override fun singleUse(singleUse: Boolean): T = applyInstance {
        this.singleUse = singleUse
    }
}