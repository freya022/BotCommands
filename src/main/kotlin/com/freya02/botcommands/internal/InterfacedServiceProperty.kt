package com.freya02.botcommands.internal

import com.freya02.botcommands.api.core.service.ServiceContainer
import com.freya02.botcommands.api.core.service.getServiceOrNull
import mu.KotlinLogging

inline fun <reified T : Any, U : T> ServiceContainer.interfacedService(crossinline defaultSupplier: () -> U): Lazy<T> {
    val logger = KotlinLogging.logger { }
    return lazy {
        (getServiceOrNull<T>() ?: defaultSupplier()).also { service ->
            logger.debug { "Found an instance of interfaced service ${T::class.simpleName} (${service::class.simpleNestedName})" }
        }
    }
}

inline fun <reified T : Any> ServiceContainer.nullableInterfacedService(): Lazy<T?> {
    val logger = KotlinLogging.logger { }
    return lazy {
        getServiceOrNull<T>().also { service ->
            if (service != null) {
                logger.debug { "Found an instance of interfaced service ${T::class.simpleNestedName} (${service::class.simpleNestedName})" }
            } else {
                logger.debug { "Found no instance of interfaced service ${T::class.simpleNestedName}" }
            }
        }
    }
}