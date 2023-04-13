package com.freya02.botcommands.internal

import com.freya02.botcommands.api.core.ServiceContainer
import mu.KotlinLogging
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

inline fun <T : Any, reified V : Any> ServiceContainer.interfacedService(crossinline defaultSupplier: () -> V): ReadOnlyProperty<T, V> {
    return object : ReadOnlyProperty<T, V> {
        private val logger = KotlinLogging.logger { }
        private val value by lazy {
            (getServiceOrNull<V>() ?: defaultSupplier()).also {
                logger.info { "Using ${it::class.java.simpleName} as a ${V::class.java.simpleName} instance" }
            }
        }

        override fun getValue(thisRef: T, property: KProperty<*>): V {
            return value
        }
    }
}