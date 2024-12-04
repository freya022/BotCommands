package io.github.freya022.botcommands.internal.core.hooks

import io.github.freya022.botcommands.api.core.annotations.BEventListener
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

internal class EventListenerList {

    // Only protect modification operations, traversal is fine
    private val lock = ReentrantLock()
    private var map: Map<BEventListener.RunMode, MutableList<EventHandlerFunction>> = emptyMap()

    operator fun get(mode: BEventListener.RunMode): List<EventHandlerFunction>? = map[mode]

    inline fun <R> map(block: (EventHandlerFunction) -> R): List<R> = map.values.flatten().map(block)

    fun add(t: EventHandlerFunction): Unit = lock.withLock {
        val newMap = newMap()
        newMap.getOrPut(t.runMode) { arrayListOf() }.add(t)

        for (handlers in newMap.values) {
            handlers.sortWith(EventHandlerFunction.Companion.priorityComparator)
        }

        this.map = newMap
    }

    fun removeAll(removedList: EventListenerList): Boolean = lock.withLock {
        val newMap = newMap()

        var removedAny = false
        removedList.map.forEach { (mode, handlers) ->
            val newHandlers = newMap[mode] ?: return@forEach
            removedAny = removedAny || newHandlers.removeAll(handlers)
        }

        this.map = newMap
        return removedAny
    }

    private fun newMap() = EnumMap<BEventListener.RunMode, MutableList<EventHandlerFunction>>(BEventListener.RunMode::class.java).apply {
        map.forEach { (mode, handlers) ->
            put(mode, handlers.toMutableList() /* copy */)
        }
    }
}