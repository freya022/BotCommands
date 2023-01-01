package com.freya02.botcommands.internal.core

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.core.EventDispatcher
import com.freya02.botcommands.api.core.ServiceStart
import com.freya02.botcommands.api.core.annotations.BEventListener
import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.internal.core.events.BStatusChangeEvent
import mu.KotlinLogging
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.StatusChangeEvent
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@BService(start = ServiceStart.LAZY)
class JDAInitListener(private val context: BContext) {
    private val logger = KotlinLogging.logger { }
    private val lock = ReentrantLock()

    @BEventListener //The first event JDA dispatches is a INITIALIZED StatusChangeEvent
    fun onJDAStatusChange(event: StatusChangeEvent) {
        if (event.newStatus == JDA.Status.INITIALIZED && context.status != BContext.Status.READY) {
            lock.withLock {
                val exception = IllegalStateException("""
                A JDA instance was constructed before the framework had finished initializing
                Possible solutions include:
                    - Building JDA after BBuilder has returned
                    - Building JDA in a service annotated with @${BService::class.simpleName}(start = ServiceStart.READY)
            """.trimIndent())
                logger.error("An exception occurred while initializing the framework", exception)

                Runtime.getRuntime().halt(112) //No choice, the events are async and can't stop initialization
            }
        }
    }

    @BEventListener
    fun onBStatusChange(event: BStatusChangeEvent, eventDispatcher: EventDispatcher) {
        if (event.newStatus == BContext.Status.READY) { //This listener isn't needed anymore after the framework is ready
            eventDispatcher.removeEventListener(this)
        }
    }
}