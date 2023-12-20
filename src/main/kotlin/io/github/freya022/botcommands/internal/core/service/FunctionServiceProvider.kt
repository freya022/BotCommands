package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.core.service.ServiceError
import io.github.freya022.botcommands.api.core.service.annotations.Primary
import io.github.freya022.botcommands.api.core.utils.getSignature
import io.github.freya022.botcommands.internal.utils.shortSignatureNoSrc
import io.github.freya022.botcommands.internal.utils.throwInternal
import kotlin.reflect.KFunction
import kotlin.reflect.full.hasAnnotation
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
        if (instance != null)
            throwInternal("Tried to create an instance using ${function.shortSignatureNoSrc} when one already exists, instance should be retrieved manually beforehand")

        // Definitely an error if an instance is trying to be created
        // before we know if it's instantiable.
        // We know it's instantiable when the error is null, throw if non-null
        serviceError?.let { serviceError ->
            throwInternal("""
                Tried to create an instance while a service error exists / hasn't been determined
                Provider: $providerKey
                Instance: $instance
                Error: ${serviceError.toSimpleString()}
            """.trimIndent())
        }

        val timedInstantiation = function.callConstructingFunction(serviceContainer)
        if (timedInstantiation.result.serviceError != null)
            return timedInstantiation

        return timedInstantiation.also { instance = it.result.getOrThrow() }
    }

    override fun toString() = providerKey
}

internal fun KFunction<*>.getServiceName(): String = getAnnotatedServiceName() ?: this.name