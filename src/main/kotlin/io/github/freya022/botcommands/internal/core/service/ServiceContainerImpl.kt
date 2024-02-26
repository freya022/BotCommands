package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.commands.annotations.Optional
import io.github.freya022.botcommands.api.core.service.*
import io.github.freya022.botcommands.api.core.service.ServiceError.ErrorType.*
import io.github.freya022.botcommands.api.core.service.annotations.MissingServiceMessage
import io.github.freya022.botcommands.api.core.service.annotations.ServiceName
import io.github.freya022.botcommands.api.core.utils.*
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.service.provider.ClassServiceProvider
import io.github.freya022.botcommands.internal.core.service.provider.ProviderName
import io.github.freya022.botcommands.internal.core.service.provider.ServiceProvider
import io.github.freya022.botcommands.internal.utils.*
import io.github.freya022.botcommands.internal.utils.ReflectionMetadata.sourceFile
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.declaringClass
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.function
import io.github.oshai.kotlinlogging.KotlinLogging
import java.lang.reflect.AnnotatedParameterizedType
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.jvm.internal.CallableReference
import kotlin.reflect.*
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.safeCast
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName
import kotlin.time.DurationUnit

internal class ServiceCreationStack {
    private val localSet: ThreadLocal<MutableSet<ProviderName>> = ThreadLocal.withInitial { linkedSetOf() }
    private val set get() = localSet.get()

    internal operator fun contains(provider: ServiceProvider) = set.contains(provider.providerKey)

    //If services have circular dependencies during checking, consider it to not be an issue
    internal inline fun <R> withServiceCheckKey(provider: ServiceProvider, block: () -> R): R? {
        if (!set.add(provider.providerKey)) return null
        try {
            return block()
        } finally {
            set.remove(provider.providerKey)
        }
    }

    internal inline fun <R> withServiceCreateKey(provider: ServiceProvider, block: () -> R): R {
        if (!set.add(provider.providerKey))
            throw IllegalStateException("Circular dependency detected, list of the services being created : [${set.joinToString(" -> ")}] ; attempted to create ${provider.providerKey}")
        try {
            return block()
        } finally {
            set.remove(provider.providerKey)
        }
    }
}

private val logger = KotlinLogging.logger<ServiceContainer>()

internal class ServiceContainerImpl internal constructor(internal val context: BContextImpl) : ServiceContainer {
    private val lock = ReentrantLock()
    private val serviceCreationStack = ServiceCreationStack()

    internal fun loadServices() {
        getLoadableService()
            .mapTo(sortedSetOf()) { clazz ->
                context.serviceProviders.findForType(clazz)
                    ?: throwInternal("Unable to find back service provider for ${clazz.jvmName}")
            }
            .forEach { provider -> tryGetService<Any>(provider).getOrThrow() }
    }

    override fun <T : Any> peekServiceOrNull(clazz: KClass<T>): T? = lock.withLock {
        val providers = context.serviceProviders.findAllForType(clazz)
        if (providers.isEmpty())
            return null

        val providerResult = getInstantiablePrimaryProvider(clazz, providers)
        val provider = providerResult.service
        val providerError = providerResult.serviceError

        if (providerError != null) {
            val (errorType, errorMessage) = providerError
            if (errorType == NON_UNIQUE_PROVIDERS) {
                logger.debug { errorMessage }
            } else {
                logger.trace { "Peeking service ${clazz.simpleNestedName} error: $errorMessage" }
            }
            return null
        }

        return when {
            provider != null -> provider.instance?.let(clazz::cast)
            else -> throwInternal("No error yet no provider is present")
        }
    }

    override fun <T : Any> peekServiceOrNull(name: String, requiredType: KClass<T>): T? = lock.withLock {
        val provider = context.serviceProviders.findForName(name) ?: return null
        if (!provider.primaryType.isSubclassOf(requiredType)) return null

        return provider.instance?.let { requiredType.safeCast(it) }
    }

    override fun <T : Any> tryGetService(name: String, requiredType: KClass<T>): ServiceResult<T> = lock.withLock {
        val provider = context.serviceProviders.findForName(name)
            ?: return NO_PROVIDER.toResult("No service or factories found for service name '$name'")
        if (!provider.primaryType.isSubclassOf(requiredType))
            return provider.createInvalidTypeError(requiredType)

        return tryGetService(provider)
    }

    override fun <T : Any> tryGetService(clazz: KClass<T>): ServiceResult<T> = lock.withLock {
        val providers = context.serviceProviders.findAllForType(clazz)

        val providerResult = getInstantiablePrimaryProvider(clazz, providers)
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

        return createService(provider)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> createService(provider: ServiceProvider): ServiceResult<T> = lock.withLock {
        try {
            return serviceCreationStack.withServiceCreateKey(provider) {
                //Don't measure time globally, we need to not take into account the time to make dependencies
                val (anyResult, duration) = provider.createInstance(this)
                //Doesn't really matter, the object is not used anyway
                val result: ServiceResult<T> = anyResult as ServiceResult<T>
                if (result.serviceError != null)
                    return result

                val instance = result.getOrThrow()
                if (!provider.primaryType.isInstance(instance))
                    throwInternal("Provider primary type is ${provider.primaryType.jvmName} but instance is of type ${instance.javaClass.name}, provider: ${provider.providerKey}")

                logger.trace {
                    val loadedAsTypes = provider.types.joinToString(prefix = "[", postfix = "]") { it.simpleNestedName }
                    "Loaded service ${instance.javaClass.simpleNestedName} as $loadedAsTypes in ${duration.toString(DurationUnit.MILLISECONDS, decimals = 3)}"
                }
                ServiceResult.pass(instance)
            }
        } catch (e: Exception) {
            throw RuntimeException("Unable to create service ${provider.primaryType.simpleNestedName}", e)
        }
    }

    private fun <T : Any> ServiceProvider.createInvalidTypeError(requiredType: KClass<T>): ServiceResult<T> =
        INVALID_TYPE.toResult(
            errorMessage = "A service was found but type is incorrect, " +
                    "requested: ${requiredType.simpleNestedName}, actual: ${this.primaryType.simpleNestedName}",
            extraMessage = "provider: ${this.providerKey}"
        )

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getInterfacedServiceTypes(clazz: KClass<T>): List<KClass<T>> {
        return context.serviceProviders.findAllForType(clazz).map { it.primaryType as KClass<T> }
    }

    private val interfacedServiceErrors: MutableSet<String> = ConcurrentHashMap.newKeySet()
    override fun <T : Any> getInterfacedServices(clazz: KClass<T>): List<T> {
        return context.serviceProviders
            .findAllForType(clazz)
            // Avoid circular dependency, we can't supply ourselves
            .filterNot { it in serviceCreationStack }
            .mapNotNull {
                val serviceResult = tryGetService<T>(it)
                serviceResult.serviceError?.let { serviceError ->
                    val warnMessage = buildString {
                        append("Could not create interfaced service ${clazz.simpleNestedName} with implementation ${it.primaryType.simpleNestedName} (from ${it.providerKey})")

                        if (logger.isTraceEnabled()) {
                            append("\n")
                            append(serviceError.toDetailedString())
                        } else {
                            serviceError.appendPostfixSimpleString()
                        }
                    }
                    if (interfacedServiceErrors.add(warnMessage)) {
                        if (logger.isTraceEnabled()) {
                            logger.trace { warnMessage }
                        } else {
                            logger.debug { warnMessage }
                        }
                    }
                }
                serviceResult.getOrNull()
            }
    }

    override fun <T : Any> putServiceAs(t: T, clazz: KClass<out T>, name: String?) {
        if (!clazz.isInstance(t))
            throwUser("${t.javaClass.name} is not an instance of ${clazz.jvmName}")
        context.serviceProviders.putServiceProvider(ClassServiceProvider.fromInstance(clazz, t))
    }

    override fun canCreateService(name: String, requiredType: KClass<*>): ServiceError? {
        val provider = context.serviceProviders.findForName(name)
            ?: return NO_PROVIDER.toError("No service or factories found for service name '$name'")
        if (!provider.primaryType.isSubclassOf(requiredType))
            return provider.createInvalidTypeError(requiredType).serviceError
        return canCreateService(provider)
    }

    override fun canCreateService(clazz: KClass<*>): ServiceError? {
        val providers = context.serviceProviders.findAllForType(clazz)

        val providerResult = getInstantiablePrimaryProvider(clazz, providers)
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
    private fun getInstantiablePrimaryProvider(clazz: KClass<*>, providers: Collection<ServiceProvider>): ServiceResult<ServiceProvider> {
        if (providers.isEmpty())
            return NO_PROVIDER.toResult(
                errorMessage = "No service or factories found for type ${clazz.simpleNestedName}",
                extraMessage = clazz.findAnnotation<MissingServiceMessage>()?.message
            )

        // Get instantiable providers, otherwise their errors
        val errors: MutableList<ServiceError> = arrayListOf()
        val instantiableProviders: MutableList<ServiceProvider> = arrayListOf()
        for (provider in providers) {
            when (val serviceError = canCreateService(provider)) {
                null -> instantiableProviders += provider
                else -> errors += serviceError
            }
        }

        // If there's no primary provider, take all of them
        val primaryProviders = instantiableProviders.filter { it.isPrimary }.ifEmpty { instantiableProviders }

        if (primaryProviders.size > 1) {
            return NON_UNIQUE_PROVIDERS.toResult(primaryProviders.createNonUniqueProvidersMessage(clazz))
        } else if (errors.isEmpty()) {
            val primaryProvider = primaryProviders.singleOrNull()
                ?: throwInternal("This collection cannot be empty as instantiable providers shouldn't be empty when there's no error, due to there being at least one provider")
            return ServiceResult.pass(primaryProvider)
        } else { // One provider at most, or none and has errors
            val primaryProvider = primaryProviders.firstOrNull()
                ?: return NO_USABLE_PROVIDER.toResult(
                    errorMessage = "All providers returned an error for type ${clazz.simpleNestedName}",
                    nestedError = ServiceError.fromErrors(errors)
                )

            return ServiceResult.pass(primaryProvider)
        }
    }

    private fun <T : Any> List<ServiceProvider>.createNonUniqueProvidersMessage(clazz: KClass<T>) = """
        Requested service of type '${clazz.simpleNestedName}' had multiple providers:
        ${this.joinAsList {
        buildString {
            if (it.isPrimary) append("[Primary] ")
            append("${it.primaryType.simpleNestedName} (${it.providerKey})")
        }
    }}
    """.trimIndent()

    internal fun canCreateService(provider: ServiceProvider): ServiceError? {
        if (provider.instance != null) return null

        return serviceCreationStack.withServiceCheckKey(provider) {
            provider.canInstantiate(this)
        }
    }

    private fun getLoadableService() = context.instantiableServiceAnnotationsMap
        .getAllInstantiableClasses()
        .filterNot { it.hasAnnotation<io.github.freya022.botcommands.api.core.service.annotations.Lazy>() }
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
    val requestedMandatoryName = parameter.findAnnotation<ServiceName>()?.value
    return if (requestedMandatoryName != null) {
        if (type.jvmErasure == List::class) {
            logger.warn { "Using ${annotationRef<ServiceName>()} on a list of interfaced services is ineffective on '${parameter.bestName}' of ${parameter.function.shortSignature}" }
        }
        tryGetWrappedNamedService(type, parameter, requestedMandatoryName)
    } else {
        parameter.name?.let { parameterName ->
            // Return the service retrieved via the parameter name if it exists
            tryGetWrappedNamedService(type, parameter, parameterName).onService { return this }
        }

        // If no service by parameter name was found, try by type
        when (type.jvmErasure) {
            Lazy::class -> {
                val (elementErasure, isNullable) = getLazyElementErasure(parameter)
                ServiceResult.pass(if (isNullable) lazyOrNull(elementErasure) else lazy(elementErasure))
            }
            List::class -> ServiceResult.pass(getInterfacedServices(type.findErasureOfAt<List<*>>(0).jvmErasure))
            else -> tryGetService(type.jvmErasure)
        }
    }
}

private fun ServiceContainer.tryGetWrappedNamedService(
    type: KType,
    parameter: KParameter,
    name: String
) = when (type.jvmErasure) {
    Lazy::class -> {
        val (elementErasure, isNullable) = getLazyElementErasure(parameter)
        ServiceResult.pass(if (isNullable) lazyOrNull(name, elementErasure) else lazy(name, elementErasure))
    }

    List::class -> ServiceResult.pass(getInterfacedServices(type.findErasureOfAt<List<*>>(0).jvmErasure))

    else -> tryGetService(name, type.jvmErasure)
}

private fun getLazyElementErasure(kParameter: KParameter): Pair<KClass<*>, Boolean> {
    // TODO Simplify then https://youtrack.jetbrains.com/issue/KT-63929 is fixed

    // Due to https://youtrack.jetbrains.com/issue/KT-63929
    // we need to get the annotations from the java type as they are not read by kotlin-reflect
    val elementType = kParameter.type.arguments[0].type
        ?: throwUser("Star projections cannot be used in lazily injected service")
    val function = kParameter.function
    return if (!function.declaringClass.sourceFile.endsWith(".kt")) {
        if (function is CallableReference)
            throwInternal("Cannot find lazy element nullability on a callable reference")

        val parameter = function.javaMethodOrConstructor.parameters[kParameter.index - 1] // -1 for instance parameter
        val lazyType = parameter.annotatedType as? AnnotatedParameterizedType
            ?: throwInternal("Unknown annotated type ${parameter.annotatedType.javaClass}")
        val annotatedElementType = lazyType.annotatedActualTypeArguments.singleOrNull()
            ?: throwInternal("No single argument in annotated type $lazyType: ${lazyType.annotatedActualTypeArguments.contentToString()}")
        val annotations = annotatedElementType.annotations
        val isNullable = annotations.any { it.annotationClass.jvmName.endsWith("Nullable") }
                || annotations.any { it.annotationClass == Optional::class }

        elementType.jvmErasure to isNullable
    } else {
        val isNullable = elementType.isMarkedNullable || elementType.hasAnnotation<Optional>()
        elementType.jvmErasure to isNullable
    }
}
