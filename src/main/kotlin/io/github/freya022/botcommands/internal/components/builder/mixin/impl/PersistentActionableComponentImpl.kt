@file:Suppress("DEPRECATION")

package io.github.freya022.botcommands.internal.components.builder.mixin.impl

import io.github.freya022.botcommands.api.components.builder.IPersistentActionableComponent
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.components.builder.AbstractActionableComponent
import io.github.freya022.botcommands.internal.components.builder.InstanceRetriever
import io.github.freya022.botcommands.internal.components.builder.mixin.IPersistentActionableComponentMixin
import io.github.freya022.botcommands.internal.components.handler.PersistentHandler

internal class PersistentActionableComponentImpl<T : IPersistentActionableComponent<T>> internal constructor(
    context: BContext,
    instanceRetriever: InstanceRetriever<T>
) : AbstractActionableComponent<T>(context, instanceRetriever),
    IPersistentActionableComponentMixin<T> {

    override var handler: PersistentHandler? = null
        private set

    override fun bindTo(handlerName: String, data: List<Any?>): T = applyInstance {
        this.handler = PersistentHandler.create(handlerName, data)
    }
}