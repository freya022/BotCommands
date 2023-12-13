package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.core.service.ServiceError
import io.github.freya022.botcommands.api.core.utils.getSignature
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.jvmErasure

internal class FunctionServiceProvider(
    private val function: KFunction<*>,
    override var instance: Any? = null
) : ServiceProvider {
    override val name = function.getServiceName()
    override val providerKey = function.getSignature(source = false, qualifiedClass = true, qualifiedTypes = true)
    override val primaryType get() = function.returnType.jvmErasure
    override val types = function.getServiceTypes(primaryType)
    override val priority = function.getAnnotatedServicePriority()

    /**
     * If not the sentinel value, the service was attempted to be created.
     */
    private var serviceError: ServiceError? = ServiceProvider.nullServiceError

    override fun canInstantiate(serviceContainer: ServiceContainerImpl): ServiceError? {
        // Returns null if there is no error, the error itself if there's one
        if (serviceError !== ServiceProvider.nullServiceError) return serviceError

        serviceError = checkInstantiate(serviceContainer)
        return serviceError
    }

    private fun checkInstantiate(serviceContainer: ServiceContainerImpl): ServiceError? {
        function.commonCanInstantiate(serviceContainer, primaryType)?.let { serviceError -> return serviceError }
        function.checkConstructingFunction(serviceContainer)?.let { serviceError -> return serviceError }

        return null
    }

    override fun createInstance(serviceContainer: ServiceContainerImpl): TimedInstantiation {
        val timedInstantiation = function.callConstructingFunction(serviceContainer)
        if (timedInstantiation.result.serviceError != null)
            return timedInstantiation

        return timedInstantiation.also { instance = it.result.getOrThrow() }
    }

    override fun toString() = providerKey
}

internal fun KFunction<*>.getServiceName(): String = getAnnotatedServiceName() ?: this.name