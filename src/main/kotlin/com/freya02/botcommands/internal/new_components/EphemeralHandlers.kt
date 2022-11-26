package com.freya02.botcommands.internal.new_components

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

internal abstract class EphemeralHandlers<T> {
    private val lock = ReentrantLock()
    private val map = hashMapOf<Long, T>()
    private var currentId: Long = 0

    operator fun get(handlerId: Long): T? = map[handlerId]

    fun put(handler: T) = lock.withLock {
        val id = currentId++
        map[id] = handler
        return@withLock id
    }

    fun remove(handlerId: Long) {
        map.remove(handlerId)
    }
}