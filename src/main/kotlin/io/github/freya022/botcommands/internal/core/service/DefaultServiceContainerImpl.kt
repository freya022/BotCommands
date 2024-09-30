package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.core.config.BServiceConfig
import io.github.freya022.botcommands.api.core.service.*
import io.github.freya022.botcommands.api.core.service.ServiceError.ErrorType.*
import io.github.freya022.botcommands.api.core.service.annotations.MissingServiceMessage
import io.github.freya022.botcommands.api.core.service.annotations.ServiceName
import io.github.freya022.botcommands.api.core.utils.*
import io.github.freya022.botcommands.internal.core.exceptions.ServiceException
import io.github.freya022.botcommands.internal.core.service.provider.ProvidedServiceProvider
import io.github.freya022.botcommands.internal.core.service.provider.ServiceProvider
import io.github.freya022.botcommands.internal.core.service.provider.ServiceProviders
import io.github.freya022.botcommands.internal.core.service.provider.TimedInstantiation
import io.github.freya022.botcommands.internal.core.service.stack.DefaultServiceCreationStack
import io.github.freya022.botcommands.internal.core.service.stack.TracedServiceCreationStack
import io.github.freya022.botcommands.internal.utils.*
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.declaringClass
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.function
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.cast
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName

private val logger = KotlinLogging.loggerOf<ServiceContainer>()

internal class DefaultServiceContainerImpl internal constructor(internal val serviceBootstrap: DefaultBotCommandsBootstrap) : DefaultServiceContainer {
    internal val serviceConfig: BServiceConfig get() = serviceBootstrap.serviceConfig
    internal val serviceProviders: ServiceProviders get() = serviceBootstrap.serviceProviders
    private val lock = ReentrantLock()
    private val serviceCreationStack = when {
        serviceConfig.debug -> TracedServiceCreationStack()
        else -> DefaultServiceCreationStack()
    }

    internal fun loadServices() {
        getService<DefaultInstantiableServices>()
            .availableProviders
            .filterNot { it.isLazy }
            // This should never throw as the providers are available and not lazy
            .forEach { provider -> tryGetService<Any>(provider).getOrThrow() }
    }

    override fun <T : Any> peekServiceOrNull(clazz: KClass<T>): T? = lock.withLock {
        peekServiceOrNull(clazz, null, serviceProviders.findAllForType(clazz))
    }

    override fun <T : Any> peekServiceOrNull(name: String, requiredType: KClass<T>): T? = lock.withLock {
        peekServiceOrNull(requiredType, name, serviceProviders.findAllForName(name))
    }

    private fun <T : Any> peekServiceOrNull(clazz: KClass<T>, name: String?, providers: Collection<ServiceProvider>): T? {
        if (providers.isEmpty())
            return null

        val providerResult = getInstantiablePrimaryProvider(clazz, name, providers)
        val provider = providerResult.service
        val providerError = providerResult.serviceError

        if (providerError != null) {
            val (errorType, errorMessage) = providerError
            if (errorType == NON_UNIQUE_PROVIDERS) {
                logger.debug { errorMessage }
            } else {
                if (name != null) {
                    logger.trace { "Peeking service '$name' error: $errorMessage" }
                } else {
                    logger.trace { "Peeking service ${clazz.simpleNestedName} error: $errorMessage" }
                }
            }
            return null
        }

        return when {
            provider != null -> provider.instance?.let(clazz::cast)
            else -> throwInternal("No error yet no provider is present")
        }
    }

    override fun <T : Any> tryGetService(name: String, requiredType: KClass<T>): ServiceResult<T> = lock.withLock {
        tryGetService(requiredType, name, serviceProviders.findAllForName(name))
    }

    override fun <T : Any> tryGetService(clazz: KClass<T>): ServiceResult<T> = lock.withLock {
        tryGetService(clazz, null, serviceProviders.findAllForType(clazz))
    }

    private fun <T : Any> tryGetService(clazz: KClass<T>, name: String?, providers: Collection<ServiceProvider>): ServiceResult<T> {
        val providerResult = getInstantiablePrimaryProvider(clazz, name, providers)
        val provider = providerResult.service

        return when {
            provider != null -> tryGetService(provider)
            else -> ServiceResult.fail(providerResult.serviceError ?: throwInternal("Can't have no provider and no error"))
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> tryGetService(provider: ServiceProvider): ServiceResult<T> {
        val instance = provider.instance as T?
        if (instance != null) {
            return ServiceResult.pass(instance)
        }

        val serviceError = canCreateService(provider)
        if (serviceError != null)
            return ServiceResult.fail(serviceError)

        return ServiceResult.pass(createService(provider))
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> createService(provider: ServiceProvider): T = lock.withLock {
        try {
            val instance =  serviceCreationStack.withServiceCreateKey(provider) {
                provider.createInstance(this) as TimedInstantiation<T>
            }

            if (!provider.primaryType.isInstance(instance))
                throwInternal("Provider primary type is ${provider.primaryType.jvmName} but instance is of type ${instance.javaClass.name}, provider: ${provider.getProviderSignature()}")

            instance
        } catch (e: Exception) {
            e.rethrow("Unable to create service ${provider.primaryType.simpleNestedName}")
        }
    }

    override fun getServiceNamesForAnnotation(annotationType: KClass<out Annotation>): Collection<String> {
        return serviceProviders.allProviders
            .filter { it.annotations.any { a -> a.annotationClass == annotationType } }
            .map { it.name }
            .unmodifiableView()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <A : Annotation> findAnnotationOnService(name: String, annotationType: KClass<A>): A? {
        val providers = serviceProviders.findAllForName(name)
        val providerResult = getInstantiablePrimaryProvider(clazz = null, name, providers)
        val provider = providerResult.getProviderOrThrow()
        return provider.annotations.find { it.annotationClass == annotationType } as A?
    }

    private fun ServiceResult<ServiceProvider>.getProviderOrThrow(): ServiceProvider {
        if (service != null)
            return service
        throw ServiceException(serviceError ?: throwInternal("Can't have no provider and no error"))
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getInterfacedServiceTypes(clazz: KClass<T>): List<KClass<T>> {
        return serviceProviders.findAllForType(clazz)
            // Avoid circular dependency, we can't supply ourselves
            .filterNot { it in serviceCreationStack }
            .filter { it.canInstantiate(this) == null }
            .map { it.primaryType as KClass<T> }
    }

    private val failedInterfacedServices: MutableSet<KClass<*>> = ConcurrentHashMap.newKeySet()
    override fun <T : Any> getInterfacedServices(clazz: KClass<T>): List<T> {
        return serviceProviders
            .findAllForType(clazz)
            // Avoid circular dependency, we can't supply ourselves
            .filterNot { it in serviceCreationStack }
            .mapNotNull {
                val serviceResult = tryGetService<T>(it)
                serviceResult.serviceError?.let { serviceError ->
                    if (failedInterfacedServices.add(it.primaryType)) {
                        fun createDebugMessage(): String = buildString {
                            append("Could not create interfaced service ${clazz.simpleNestedName} with implementation ${it.primaryType.simpleNestedName}")
                            serviceError.appendPostfixSimpleString()
                        }

                        fun createTraceMessage(): String = buildString {
                            appendLine("Could not create interfaced service ${clazz.simpleNestedName} with implementation ${it.primaryType.simpleNestedName} (from ${it.getProviderSignature()})")
                            append(serviceError.toDetailedString())
                        }

                        if (logger.isTraceEnabled()) {
                            logger.trace { createTraceMessage() }
                        } else {
                            logger.debug { createDebugMessage() }
                        }
                    }
                }
                serviceResult.getOrNull()
            }
    }

    override fun <T : Any> putService(
        t: T,
        clazz: KClass<out T>,
        name: String?,
        isPrimary: Boolean,
        priority: Int,
        annotations: Collection<Annotation>,
        typeAliases: Set<KClass<*>>
    ) {
        serviceProviders.putServiceProvider(ProvidedServiceProvider(t, clazz, name, isPrimary, priority, annotations, typeAliases))
    }

    override fun canCreateService(name: String, requiredType: KClass<*>): ServiceError? {
        return canCreateService(requiredType, name, serviceProviders.findAllForName(name))
    }

    override fun canCreateService(clazz: KClass<*>): ServiceError? {
        return canCreateService(clazz, null, serviceProviders.findAllForType(clazz))
    }

    private fun canCreateService(clazz: KClass<*>, name: String?, providers: Collection<ServiceProvider>): ServiceError? {
        val providerResult = getInstantiablePrimaryProvider(clazz, name, providers)
        val provider = providerResult.service

        return when {
            provider != null -> null
            else -> providerResult.serviceError ?: throwInternal("Can't have no provider and no error")
        }
    }

    /**
     * Returns the primary provider out of the [providers],
     * this only takes into account providers than can create their services.
     *
     * - **One usable provider:** Regardless of if it's primary or not, it is returned.
     *
     * - **`N` usable providers:** One of them must be a primary provider,
     * if there is no primary provider, an error is returned.
     *
     * - **No usable provider:** The result contains the error.
     */
    private fun getInstantiablePrimaryProvider(clazz: KClass<*>?, name: String?, providers: Collection<ServiceProvider>): ServiceResult<ServiceProvider> {
        if (providers.isEmpty())
            return NO_PROVIDER.toResult(
                errorMessage = "No service or factories found for ${getProviderCharacteristics(clazz, name)}",
                extraMessage = clazz?.findAnnotation<MissingServiceMessage>()?.message
            )

        val (errors, primaryProviders) = partitionPrimaryProviders(name, providers)

        if (primaryProviders.size > 1) {
            return onMultiplePrimaryProviders(clazz, name, primaryProviders)
        }

        val primaryProvider = if (errors.isEmpty()) {
            primaryProviders.singleOrNull()
                ?: throwInternal("This collection cannot be empty as instantiable providers shouldn't be empty when there's no error, due to there being at least one provider")
        } else { // One provider at most, or none and has errors
            primaryProviders.firstOrNull()
                ?: return when {
                    errors.size == 1 -> ServiceResult.fail(errors.single())
                    else -> NO_USABLE_PROVIDER.toResult(
                        errorMessage = "All providers returned an error for ${getProviderCharacteristics(clazz, name)}",
                        nestedError = ServiceError.fromErrors(errors)
                    )
                }
        }

        if (clazz != null && !primaryProvider.primaryType.isSubclassOf(clazz)) {
            val errorMessage = "A provider was found but type is incorrect, " +
                    "requested: ${clazz.simpleNestedName}, actual: ${primaryProvider.primaryType.simpleNestedName}"

            // In the case we only requested by type,
            // the caller should have already supplied type-compatible providers
            if (name == null)
                throwInternal(errorMessage)

            return INVALID_TYPE.toResult(
                errorMessage = errorMessage,
                failedFunction = primaryProvider.getProviderFunction()
            )
        }

        return ServiceResult.pass(primaryProvider)
    }

    /**
     * Returns the errors of unavailable providers, and the **primary** providers
     */
    private fun partitionPrimaryProviders(name: String?, providers: Collection<ServiceProvider>): Pair<List<ServiceError>, List<ServiceProvider>> {
        // Get instantiable providers, otherwise their errors
        val errors: MutableList<ServiceError> = arrayListOf()
        val instantiableProviders: MutableList<ServiceProvider> = arrayListOf()
        for (provider in providers) {
            when (val serviceError = canCreateService(provider)) {
                null -> instantiableProviders += provider
                else -> errors += serviceError
            }
        }

        return errors to when {
            // Filter by primary if we're getting providers by type, if there's no primary provider, take all of them
            name == null -> instantiableProviders.filter { it.isPrimary }.ifEmpty { instantiableProviders }
            // If we got providers by name, use them
            else -> instantiableProviders
        }
    }

    private fun onMultiplePrimaryProviders(clazz: KClass<*>?, name: String?, primaryProviders: List<ServiceProvider>): ServiceResult<ServiceProvider> {
        // Getting a provider by name cannot give multiple providers
        if (name != null) {
            // If DefaultInstantiableServices is still running,
            // usable providers with the same names weren't checked yet,
            // throw here too.
            if (serviceProviders.findAllForType(DefaultInstantiableServices::class).single() in serviceCreationStack) {
                throwState(duplicatedNamedProvidersMsg(mapOf(name to primaryProviders)))
            } else {
                throwInternal("${classRef<InstantiableServices>()} should have made sure that only one '$name' named provider exists")
            }
        }

        return NON_UNIQUE_PROVIDERS.toResult(
            errorMessage = "Requested service of ${getProviderCharacteristics(clazz, name = null)} had multiple providers",
            extra = mapOf("Providers" to lazy { '\n' + primaryProviders.toPrimaryProviderString().prependIndent() })
        )
    }

    private fun getProviderCharacteristics(clazz: KClass<*>?, name: String?) = when {
        clazz == null && name == null -> throwInternal("Cannot get provider characteristics with nothing")
        clazz != null && name != null -> "type ${clazz.simpleNestedName} and name '$name'"
        clazz != null -> "type ${clazz.simpleNestedName}"
        else -> "name $name"
    }

    private fun List<ServiceProvider>.toPrimaryProviderString() = joinAsList {
        buildString {
            if (it.isPrimary) append("[Primary] ")
            append(it.getProviderSignature())
        }
    }

    internal fun canCreateService(provider: ServiceProvider): ServiceError? {
        if (provider.instance != null) return null

        return serviceCreationStack.withServiceCheckKey(provider) {
            provider.canInstantiate(this)
        }
    }

}

internal fun ServiceContainer.getFunctionService(function: KFunction<*>): Any = when {
    function.isConstructor -> throwInternal(
        function,
        "Tried to get a function's instance but was a constructor, this should have been checked beforehand"
    )
    function.isStatic -> throwInternal(
        function,
        "Tried to get a function's instance but was static, this should have been checked beforehand"
    )
    else -> tryGetService(function.declaringClass).getOrThrow()
}

internal fun ServiceContainer.getFunctionServiceOrNull(function: KFunction<*>): Any? = when {
    function.isConstructor || function.isStatic -> null
    else -> tryGetService(function.declaringClass).getOrNull()
}

internal fun ServiceContainer.getParameters(types: List<KClass<*>>, map: Map<KClass<*>, Any> = mapOf()): List<Any> {
    return types.map {
        map[it] ?: getService(it)
    }
}

/**
 * NOTE: Lazy services do not get checked if they can be instantiated,
 * this aligns with the behavior of a user using `ServiceContainer.lazy`.
 */
internal fun ServiceContainer.tryGetWrappedService(parameter: KParameter): ServiceResult<*> {
    val type = parameter.type

    fun getExplicitNamedLazyService(name: String): ServiceResult<LazyService<*>> {
        val elementErasure = type.findErasureOfAt<LazyService<*>>(0).jvmErasure
        return ServiceResult.pass(lazyService(elementErasure, name))
    }

    fun getImplicitNamedLazyService(): ServiceResult<LazyService<*>> {
        val elementErasure = type.findErasureOfAt<LazyService<*>>(0).jvmErasure
        return ServiceResult.pass(ImplicitNamedLazyServiceImpl(this, elementErasure, parameter.name))
    }

    val requestedMandatoryName = parameter.findAnnotationRecursive<ServiceName>()?.value
    return if (requestedMandatoryName != null) {
        require(type.jvmErasure != List::class) {
            "Cannot use ${annotationRef<ServiceName>()} on a list of interfaced services, on '${parameter.bestName}' of ${parameter.function.shortSignature}"
        }

        when (type.jvmErasure) {
            Lazy::class -> throw UnsupportedOperationException("kotlin.Lazy is unsupported, please use LazyService instead")
            LazyService::class -> getExplicitNamedLazyService(requestedMandatoryName)
            else -> tryGetService(requestedMandatoryName, type.jvmErasure)
        }
    } else {
        // We need to try with the implicit name, and type-only
        when (type.jvmErasure) {
            List::class -> ServiceResult.pass(getInterfacedServices(type.findErasureOfAt<List<*>>(0).jvmErasure))
            Lazy::class -> throw UnsupportedOperationException("kotlin.Lazy is unsupported, please use LazyService instead")
            // Implicit name then type-only
            LazyService::class -> getImplicitNamedLazyService()
            else -> tryGetService(type.jvmErasure, parameter.name).orElse { tryGetService(type.jvmErasure) }
        }
    }
}

private inline fun <T : Any> ServiceResult<T>.orElse(crossinline block: () -> ServiceResult<T>): ServiceResult<T> = when {
    service != null -> this
    else -> block()
}

private fun <T : Any> ServiceContainer.tryGetService(type: KClass<T>, name: String?): ServiceResult<T> = when (name) {
    null -> tryGetService(type)
    else -> tryGetService(name, type)
}
