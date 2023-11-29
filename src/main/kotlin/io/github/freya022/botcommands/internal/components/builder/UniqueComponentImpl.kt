package io.github.freya022.botcommands.internal.components.builder

import io.github.freya022.botcommands.api.components.builder.IUniqueComponent

internal class UniqueComponentImpl<T : IUniqueComponent<T>> internal constructor(
    override val instanceRetriever: InstanceRetriever<T>
) : BuilderInstanceHolderImpl<T>(),
    IUniqueComponent<T> {

    override var oneUse: Boolean = false
}