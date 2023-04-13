package com.freya02.botcommands.internal

import com.freya02.botcommands.api.core.ServiceContainer
import mu.KotlinLogging

inline fun <reified V : Any> ServiceContainer.interfacedService(crossinline defaultSupplier: () -> V): Lazy<V> {
    val logger = KotlinLogging.logger { }
    return lazy {
        (getServiceOrNull() ?: defaultSupplier()).also { service ->
            logger.info { "Using ${service::class.java.simpleName} as a ${V::class.java.simpleName} instance" }
        }
    }
}

inline fun <reified V : Any> ServiceContainer.nullableInterfacedService(): Lazy<V?> {
    val logger = KotlinLogging.logger { }
    return lazy {
        getServiceOrNull<V>().also { service ->
            if (service != null) {
                logger.info { "Using ${service::class.java.simpleName} as a ${V::class.java.simpleName} instance" }
            } else {
                logger.info { "Found no instance of interfaced service ${V::class.java.simpleName}" }
            }
        }
    }
}