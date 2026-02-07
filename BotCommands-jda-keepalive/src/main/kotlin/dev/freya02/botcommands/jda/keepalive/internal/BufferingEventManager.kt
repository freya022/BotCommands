package dev.freya02.botcommands.jda.keepalive.internal

import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.hooks.IEventManager
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

internal class BufferingEventManager @DynamicCall constructor(
    delegate: IEventManager,
) : IEventManager {

    private val lock = ReentrantLock()
    private val eventBuffer: MutableList<GenericEvent> = arrayListOf()

    private var delegate: IEventManager? = delegate

    internal fun setDelegate(delegate: IEventManager) {
        lock.withLock {
            check(delegate !is BufferingEventManager) {
                "Tried to delegate to a BufferingEventManager!"
            }

            this.delegate = delegate
            eventBuffer.forEach(::handle)
        }
    }

    internal fun detach() {
        lock.withLock {
            delegate = null
        }
    }

    override fun register(listener: Any) {
        lock.withLock {
            val delegate = delegate ?: error("Should not happen, implement a listener queue if necessary")
            delegate.register(listener)
        }
    }

    override fun unregister(listener: Any) {
        lock.withLock {
            val delegate = delegate ?: error("Should not happen, implement a listener queue if necessary")
            delegate.unregister(listener)
        }
    }

    override fun handle(event: GenericEvent) {
        val delegate = lock.withLock {
            val delegate = delegate
            if (delegate == null) {
                eventBuffer += event
                return
            }
            delegate
        }

        delegate.handle(event)
    }

    override fun getRegisteredListeners(): List<Any?> {
        lock.withLock {
            val delegate = delegate ?: error("Should not happen, implement a listener queue if necessary")
            return delegate.registeredListeners
        }
    }
}
