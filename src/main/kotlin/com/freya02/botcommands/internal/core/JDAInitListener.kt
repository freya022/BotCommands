package com.freya02.botcommands.internal.core

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.core.EventDispatcher
import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.events.BStatusChangeEvent
import com.freya02.botcommands.api.core.service.ServiceStart
import com.freya02.botcommands.api.core.service.annotations.BService
import mu.KotlinLogging
import net.dv8tion.jda.api.events.Event
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@BService(start = ServiceStart.LAZY)
internal data object JDAInitListener {
    private val logger = KotlinLogging.logger { }
    private val lock = ReentrantLock()

    // Listen to any JDA event; if we received any event before this listener was unregistered,
    // then it means that JDA was started before the framework was,
    // or started in another phase than READY, in which case, signal.
    @BEventListener
    fun onJDAEvent(event: Event) {
        lock.withLock {
            val exception = IllegalStateException(
                """
                    A JDA instance was constructed before the framework had finished initializing
                    Possible solutions include:
                        - Building JDA after BBuilder has returned
                        - Building JDA in a service annotated with @${BService::class.simpleName}(start = ServiceStart.READY)
                """.trimIndent()
            )
            logger.error("An exception occurred while initializing the framework", exception)

            Runtime.getRuntime().halt(112) //No choice, the events are async and can't stop initialization
        }
    }

    @BEventListener
    fun onBStatusChange(event: BStatusChangeEvent, eventDispatcher: EventDispatcher) {
        if (event.newStatus == BContext.Status.READY) { //This listener isn't needed anymore after the framework is ready
            eventDispatcher.removeEventListener(this)
        }
    }
}