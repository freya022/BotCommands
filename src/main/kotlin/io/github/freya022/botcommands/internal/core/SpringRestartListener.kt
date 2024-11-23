package io.github.freya022.botcommands.internal.core

import io.github.freya022.botcommands.api.core.utils.awaitShutdown
import net.dv8tion.jda.api.JDA
import org.springframework.beans.factory.getBean
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.seconds

@Component
internal data object SpringRestartListener {
    @EventListener
    internal fun onContextClosed(event: ContextClosedEvent) {
        val jda = event.applicationContext.getBean<JDA>()
        jda.shutdown()

        if (!jda.awaitShutdown(10.seconds)) {
            jda.shutdownNow()
            jda.awaitShutdown()
        }
    }
}