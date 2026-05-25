package io.github.freya022.botcommands.internal.core.service.provider

import io.github.freya022.botcommands.api.core.service.ServiceError
import io.github.freya022.botcommands.api.core.service.ServiceError.ErrorType
import io.github.freya022.botcommands.api.core.service.annotations.Lazy
import io.github.freya022.botcommands.api.core.service.annotations.Primary
import io.github.freya022.botcommands.api.core.utils.getAllAnnotations
import io.github.freya022.botcommands.api.core.utils.getSignature
import io.github.freya022.botcommands.api.core.utils.isStatic
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.core.exceptions.ServiceException
import io.github.freya022.botcommands.internal.core.method.accessors.MethodAccessorFactoryProvider
import io.github.freya022.botcommands.internal.core.service.BCServiceContainerImpl
import io.github.freya022.botcommands.internal.utils.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.jvmErasure

internal class FunctionServiceProvider(
    private val declaringClass: KClass<*>,
    private val function: KFunction<*>,
) : ServiceProvider {
    override var instance: Any? = null

    override val annotations = function.getAllAnnotations()
    override val name = getServiceName(function)
    override val providerKey = function.getSignature(source = false, qualifiedClass = true, qualifiedTypes = true)
    override val primaryType get() = function.returnType.jvmErasure
    override val types = getServiceTypes(primaryType)
    override val isPrimary = hasAnnotation<Primary>()
    override val isLazy = hasAnnotation<Lazy>()
    override val priority = getAnnotatedServicePriority()

    private val isObjectFunction = declaringClass.isObject
    private val isStatic = function.isStatic

    /**
     * If not the sentinel value, the service was attempted to be created.
     */
    private var serviceError: ServiceError? = ServiceProvider.nullServiceError

    init {
        if (function.isSuspend) {
            throwArgument(function, "Service factories do not support coroutines")
        }
    }

    override fun canInstantiate(serviceContainer: BCServiceContainerImpl): ServiceError? {
        // Returns null if there is no error, the error itself if there's one
        if (serviceError !== ServiceProvider.nullServiceError) return serviceError

        val serviceError = checkInstantiate(serviceContainer)
        //Do not cache service error if a parameter is unavailable, a retrial is allowed
        when (serviceError?.errorType) {
            ErrorType.UNAVAILABLE_PARAMETER, ErrorType.UNAVAILABLE_DEPENDENCY -> {}

            else -> this.serviceError = serviceError
        }

        return serviceError
    }

    private fun checkInstantiate(serviceContainer: BCServiceContainerImpl): ServiceError? {
        checkConditions(serviceContainer, function, primaryType)?.let { serviceError -> return serviceError }
        function.checkConstructingFunction(serviceContainer)?.let { serviceError -> return serviceError }

        if (!isObjectFunction && !isStatic) {
            serviceContainer.canCreateService(declaringClass)?.let { serviceError ->
                return ErrorType.UNAVAILABLE_INSTANCE.toError(
                    errorMessage = "The '${declaringClass.simpleNestedName}' instance required by the service factory was unavailable",
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

    override fun createInstance(serviceContainer: BCServiceContainerImpl): TimedInstantiation<*> {
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

        val timedInstantiation = callConstructingFunction(serviceContainer)
        instance = timedInstantiation.instance
        return timedInstantiation
    }

    private fun callConstructingFunction(serviceContainer: BCServiceContainerImpl): TimedInstantiation<*> {
        val instance: Any? = if (isObjectFunction) {
            declaringClass.objectInstance!!
        } else if (isStatic) {
            null
        } else {
            serviceContainer.tryGetService(declaringClass).getOrThrow {
                throwArgument(function, "Could not run function as it is not static, the declaring class isn't an object, and service creation failed:\n${it.toDetailedString()}")
            }
        }

        val accessor = MethodAccessorFactoryProvider.getAccessorFactory().create(instance, function)
        val args = function.getDependencyValues(serviceContainer, accessor)

        return TimedInstantiation.of {
            accessor.call(args)
                ?: throw ServiceException(ErrorType.PROVIDER_RETURNED_NULL.toError(
                    errorMessage = "Service factory returned null",
                    failedFunction = function
                ))
        }
    }

    override fun getProviderFunction(): KFunction<*> = function

    override fun getProviderSignature(): String = getProviderFunction().shortSignature

    override fun toString() = providerKey
}

internal fun ServiceProvider.getServiceName(kFunction: KFunction<*>): String =
    getAnnotatedServiceName()
        ?: (kFunction as? KProperty.Getter<*>)?.property?.name
        ?: kFunction.name
