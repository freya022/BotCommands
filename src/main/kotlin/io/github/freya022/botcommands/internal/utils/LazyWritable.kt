package io.github.freya022.botcommands.internal.utils

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private val NO_VALUE = Any()

@Suppress("UNCHECKED_CAST")
internal class LazyWritable<T> internal constructor(initializer: () -> T) : ReadWriteProperty<Any?, T> {
    private val lock = ReentrantLock()
    private var initializer: (() -> T)? = initializer
    private var value: T = NO_VALUE as T

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (value === NO_VALUE) {
            lock.withLock {
                if (value === NO_VALUE) {
                    value = initializer!!()
                    initializer = null
                }
            }
        }

        return value
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
        this.initializer = null
    }

    override fun toString(): String {
        return if (value == NO_VALUE) "Lazy value not initialized yet." else value.toString()
    }
}

internal fun <T> lazyWritable(initializer: () -> T): LazyWritable<T> = LazyWritable(initializer)