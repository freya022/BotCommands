package com.freya02.botcommands.internal.new_components

import com.freya02.botcommands.api.core.annotations.BService
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@BService
internal class EphemeralHandlers {
    private val lock = ReentrantLock()
    private val map = hashMapOf<Long, EphemeralHandler<*>>()
    private var currentId: Long = 0

    operator fun get(handlerId: Long): EphemeralHandler<*>? = map[handlerId]

    fun put(handler: EphemeralHandler<*>) = lock.withLock {
        val id = currentId++
        map[id] = handler
        return@withLock id
    }

    fun remove(handlerId: Long) {
        map.remove(handlerId)
    }
}