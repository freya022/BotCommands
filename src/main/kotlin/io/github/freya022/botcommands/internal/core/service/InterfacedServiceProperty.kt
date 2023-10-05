package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.getServiceOrNull
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import mu.KotlinLogging

internal inline fun <reified T : Any, U : T> ServiceContainer.interfacedService(crossinline defaultSupplier: () -> U): Lazy<T> {
    val logger = KotlinLogging.logger { }
    return lazy {
        (getServiceOrNull<T>() ?: defaultSupplier()).also { service ->
            logger.debug { "Found an instance of interfaced service ${T::class.simpleName} (${service::class.simpleNestedName})" }
        }
    }
}

internal inline fun <reified T : Any> ServiceContainer.nullableInterfacedService(): Lazy<T?> {
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