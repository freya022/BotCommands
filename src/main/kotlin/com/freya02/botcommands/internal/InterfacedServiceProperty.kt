package com.freya02.botcommands.internal

import com.freya02.botcommands.api.core.ServiceContainer
import mu.KotlinLogging

inline fun <reified T : Any, U : T> ServiceContainer.interfacedService(crossinline defaultSupplier: () -> U): Lazy<T> {
    val logger = KotlinLogging.logger { }
    return lazy {
        (getServiceOrNull<T>() ?: defaultSupplier()).also { service ->
            logger.info { "Found an instance of interfaced service ${T::class.java.simpleName} as ${service::class.java.simpleName}" }
        }
    }
}

inline fun <reified T : Any> ServiceContainer.nullableInterfacedService(): Lazy<T?> {
    val logger = KotlinLogging.logger { }
    return lazy {
        getServiceOrNull<T>().also { service ->
            if (service != null) {
                logger.info { "Found an instance of interfaced service ${T::class.java.simpleName} as ${service::class.java.simpleName}" }
            } else {
                logger.info { "Found no instance of interfaced service ${T::class.java.simpleName}" }
            }
        }
    }
}