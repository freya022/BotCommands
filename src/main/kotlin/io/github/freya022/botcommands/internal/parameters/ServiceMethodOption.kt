package io.github.freya022.botcommands.internal.parameters

import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.internal.core.options.OptionImpl
import io.github.freya022.botcommands.internal.core.options.OptionType
import io.github.freya022.botcommands.internal.core.service.tryGetWrappedService

class ServiceMethodOption internal constructor(
    optionParameter: OptionParameter,
    private val serviceContainer: ServiceContainer,
) : OptionImpl(optionParameter, OptionType.SERVICE) {
    private lateinit var cachedService: Any

    // Caches the service if:
    // 1. Is non-null
    // 2. Is not Lazy/List
    internal fun getService(): Any? {
        if (::cachedService.isInitialized) return cachedService

        val result = serviceContainer.tryGetWrappedService(kParameter)
        val service = if (isOptionalOrNullable) {
            result.getOrNull() ?: return null
        } else {
            result.getOrThrow()
        }

        // Always retry getting lazy services and lists as they may be updated later
        if (service is List<*>)
            return service
        if (service is Lazy<*>) {
            if (service.isInitialized() && service.value != null)
                cachedService = service
            return service
        } else {
            cachedService = service
            return service
        }
    }
}