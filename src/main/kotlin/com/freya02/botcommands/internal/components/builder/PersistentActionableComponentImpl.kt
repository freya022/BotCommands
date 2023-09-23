package com.freya02.botcommands.internal.components.builder

import com.freya02.botcommands.api.ReceiverConsumer
import com.freya02.botcommands.api.components.builder.IPersistentActionableComponent
import com.freya02.botcommands.api.components.builder.PersistentHandlerBuilder
import com.freya02.botcommands.internal.components.PersistentHandler

internal class PersistentActionableComponentImpl : IPersistentActionableComponent {
    override var handler: PersistentHandler? = null
        private set

    override fun bindTo(handlerName: String, block: ReceiverConsumer<PersistentHandlerBuilder>) {
        this.handler = PersistentHandlerBuilder(handlerName).apply(block).build()
    }
}