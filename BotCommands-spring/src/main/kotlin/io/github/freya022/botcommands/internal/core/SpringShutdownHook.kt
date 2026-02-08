package io.github.freya022.botcommands.internal.core

import io.github.freya022.botcommands.api.core.BContext
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
internal class SpringShutdownHook(
    private val context: BContext
) {

    @EventListener(ContextClosedEvent::class)
    internal fun onContextClosed() {
        if (!context.config.enableShutdownHook)
            return

        context.shutdownNow()
    }
}
