package io.github.freya022.botcommands.internal.components.builder

import io.github.freya022.botcommands.api.ReceiverConsumer
import io.github.freya022.botcommands.api.components.builder.IPersistentActionableComponent
import io.github.freya022.botcommands.api.components.builder.PersistentHandlerBuilder
import io.github.freya022.botcommands.internal.components.PersistentHandler

internal class PersistentActionableComponentImpl : AbstractActionableComponent(), IPersistentActionableComponent {
    override var handler: PersistentHandler? = null
        private set

    override fun bindTo(handlerName: String, block: ReceiverConsumer<PersistentHandlerBuilder>) {
        this.handler = PersistentHandlerBuilder(handlerName).apply(block).build()
    }
}