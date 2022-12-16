package com.freya02.botcommands.internal.components.repositories

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

internal abstract class EphemeralHandlers<T> {
    private val lock = ReentrantLock()
    private val map = hashMapOf<Int, T>()
    private var currentId: Int = 0

    operator fun get(handlerId: Int): T? = map[handlerId]

    fun put(handler: T) = lock.withLock {
        val id = currentId++
        map[id] = handler
        return@withLock id
    }

    fun remove(handlerId: Int) = lock.withLock {
        map.remove(handlerId)
    }
}