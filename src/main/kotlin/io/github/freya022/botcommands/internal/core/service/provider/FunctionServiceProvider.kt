package io.github.freya022.botcommands.internal.core.service.provider

import io.github.freya022.botcommands.api.core.service.ServiceError
import io.github.freya022.botcommands.api.core.service.annotations.Lazy
import io.github.freya022.botcommands.api.core.service.annotations.Primary
import io.github.freya022.botcommands.api.core.utils.getSignature
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.core.service.DefaultServiceContainerImpl
import io.github.freya022.botcommands.internal.utils.*
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.jvm.jvmErasure

internal class FunctionServiceProvider(
    private val function: KFunction<*>,
    override var instance: Any? = null
) : ServiceProvider {
    override val annotations = function.getAllAnnotations()
    override val name = getServiceName(function)
    override val providerKey = function.getSignature(source = false, qualifiedClass = true, qualifiedTypes = true)
    override val primaryType get() = function.returnType.jvmErasure
    override val types = getServiceTypes(primaryType)
    override val isPrimary = hasAnnotation<Primary>()
    override val isLazy = hasAnnotation<Lazy>()
    override val priority = getAnnotatedServicePriority()

    /**
     * If not the sentinel value, the service was attempted to be created.
     */
    private var serviceError: ServiceError? = ServiceProvider.nullServiceError

    override fun canInstantiate(serviceContainer: DefaultServiceContainerImpl): ServiceError? {
        // Returns null if there is no error, the error itself if there's one
        if (serviceError !== ServiceProvider.nullServiceError) return serviceError

        val serviceError = checkInstantiate(serviceContainer)
        //Do not cache service error if a parameter is unavailable, a retrial is allowed
        when (serviceError?.errorType) {
            ServiceError.ErrorType.UNAVAILABLE_PARAMETER, ServiceError.ErrorType.UNAVAILABLE_DEPENDENCY -> {}

            else -> this.serviceError = serviceError
        }

        return serviceError
    }

    private fun checkInstantiate(serviceContainer: DefaultServiceContainerImpl): ServiceError? {
        commonCanInstantiate(serviceContainer, primaryType)?.let { serviceError -> return serviceError }
        function.checkConstructingFunction(serviceContainer)?.let { serviceError -> return serviceError }

        function.instanceParameter?.let { instanceParameter ->
            val erasure = instanceParameter.type.jvmErasure
            // If an object, don't check if it can be created,
            // as it may not be annotated as a service but still be usable
            if (erasure.isObject)
                return@let

            serviceContainer.canCreateService(erasure)?.let { serviceError ->
                return ServiceError.ErrorType.UNAVAILABLE_INSTANCE.toError(
                    errorMessage = "The '${instanceParameter.type.simpleNestedName}' instance required by the service factory was unavailable",
                    failedFunction = function,
                    nestedError = serviceError,
                    extra = mapOf(
                        "Java hint" to "Should the factory be 'static'?",
                        "Kotlin hint" to "Should the class be an 'object' instead?",
                    )
                )
            }
        }

        return null
    }

    override fun createInstance(serviceContainer: DefaultServiceContainerImpl): TimedInstantiation<*> {
        if (instance != null)
            throwInternal("Tried to create an instance using ${function.shortSignatureNoSrc} when one already exists, instance should be retrieved manually beforehand")

        // Definitely an error if an instance is trying to be created
        // before we know if it's instantiable.
        // We know it's instantiable when the error is null, throw if non-null
        serviceError?.let { serviceError ->
            throwInternal("""
                Tried to create an instance while a service error exists / hasn't been determined
                Provider: ${getProviderSignature()}
                Error: ${serviceError.toSimpleString()}
            """.trimIndent())
        }

        val timedInstantiation = function.callConstructingFunction(serviceContainer)
        instance = timedInstantiation.instance
        return timedInstantiation
    }

    override fun getProviderFunction(): KFunction<*> = function

    override fun getProviderSignature(): String = getProviderFunction().shortSignature

    override fun toString() = providerKey
}

internal fun ServiceProvider.getServiceName(kFunction: KFunction<*>): String =
    getAnnotatedServiceName()
        ?: (kFunction as? KProperty.Getter<*>)?.property?.name
        ?: kFunction.name