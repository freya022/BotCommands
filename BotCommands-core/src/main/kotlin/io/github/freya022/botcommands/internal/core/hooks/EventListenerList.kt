package io.github.freya022.botcommands.internal.core.hooks

import io.github.freya022.botcommands.api.core.annotations.BEventListener.RunMode
import io.github.freya022.botcommands.api.core.utils.enumMapOf
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

internal class EventListenerList {

    // Only protect modification operations, traversal is fine
    private val lock = ReentrantLock()
    private var map: Map<RunMode, List<EventHandlerFunction>> = emptyMap()

    val isEmpty: Boolean get() = map.isEmpty()

    operator fun get(mode: RunMode): List<EventHandlerFunction>? = map[mode]

    inline fun <R> map(block: (EventHandlerFunction) -> R): List<R> = map.values.flatten().map(block)

    fun add(t: EventHandlerFunction): Unit = lock.withLock {
        val newMap = newMap()
        newMap.getOrPut(t.runMode) { arrayListOf() }.add(t)

        for (handlers in newMap.values) {
            handlers.sortWith(EventHandlerFunction.priorityComparator)
        }

        this.map = newMap
    }

    fun removeAll(handlers: Collection<EventHandlerFunction>) = lock.withLock {
        val newMap = newMap()

        // Likely more efficient to do bulk operations on a few lists than getting the right list and removing items one by one
        newMap.values.forEach { newHandlers -> newHandlers.removeAll(handlers) }

        // Remove entries containing an empty list
        RunMode.entries.forEach { mode -> newMap.remove(mode, emptyList()) }

        this.map = newMap
    }

    private fun newMap() = enumMapOf<RunMode, MutableList<EventHandlerFunction>>().apply {
        map.forEach { (mode, handlers) ->
            put(mode, handlers.toMutableList() /* copy */)
        }
    }
}
