package io.github.freya022.botcommands.internal.core.service.stack

import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.ServiceError
import io.github.freya022.botcommands.api.core.utils.loggerOf
import io.github.freya022.botcommands.internal.core.service.provider.Instance
import io.github.freya022.botcommands.internal.core.service.provider.ServiceProvider
import io.github.freya022.botcommands.internal.core.service.provider.TimedInstantiation
import io.github.oshai.kotlinlogging.KotlinLogging

internal interface ServiceCreationStack {
    operator fun contains(provider: ServiceProvider): Boolean

    fun withServiceCheckKey(provider: ServiceProvider, block: () -> ServiceError?): ServiceError?

    fun <R : Instance> withServiceCreateKey(provider: ServiceProvider, block: () -> TimedInstantiation<R>): R

    companion object {
        val logger = KotlinLogging.loggerOf<ServiceContainer>()
    }
}
