package io.github.freya022.botcommands.internal.components.builder

import io.github.freya022.botcommands.api.ReceiverConsumer
import io.github.freya022.botcommands.api.components.builder.IPersistentActionableComponent
import io.github.freya022.botcommands.api.components.builder.PersistentHandlerBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.components.handler.PersistentHandler

internal class PersistentActionableComponentImpl<T : IPersistentActionableComponent<T>> internal constructor(
    context: BContext,
    instanceRetriever: InstanceRetriever<T>
) : AbstractActionableComponent<T>(context, instanceRetriever),
    IPersistentActionableComponent<T> {

    override var handler: PersistentHandler? = null
        private set

    override fun bindTo(handlerName: String, block: ReceiverConsumer<PersistentHandlerBuilder>): T = instance.also {
        this.handler = PersistentHandlerBuilder(handlerName).apply(block).build()
    }
}