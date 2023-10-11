package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.ServiceError
import io.github.freya022.botcommands.api.core.service.ServiceError.ErrorType.INVALID_TYPE
import io.github.freya022.botcommands.api.core.service.ServiceError.ErrorType.NO_PROVIDER
import io.github.freya022.botcommands.api.core.service.ServiceResult
import io.github.freya022.botcommands.api.core.service.ServiceStart
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.isConstructor
import io.github.freya022.botcommands.api.core.utils.isStatic
import io.github.freya022.botcommands.api.core.utils.logger
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.declaringClass
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.cast
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.safeCast
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

class ServiceContainerImpl internal constructor(internal val context: BContextImpl) : ServiceContainer {
    private val lock = ReentrantLock()
    private val serviceCreationStack = ServiceCreationStack()

    internal fun loadServices(requestedStart: ServiceStart) {
        getLoadableService(requestedStart).forEach { clazz -> getService(clazz) }
    }

    override fun <T : Any> peekServiceOrNull(clazz: KClass<T>): T? = lock.withLock {
        val providers = context.serviceProviders.findAllForType(clazz)
        if (providers.isEmpty())
            return null

        providers.forEach { provider ->
            provider.instance?.let { instance -> return clazz.cast(instance) }
        }

        return null
    }

    override fun <T : Any> peekServiceOrNull(name: String, requiredType: KClass<T>): T? = lock.withLock {
        val provider = context.serviceProviders.findForName(name) ?: return null
        return provider.instance?.let { requiredType.safeCast(it) }
    }

    override fun <T : Any> tryGetService(name: String, requiredType: KClass<T>): ServiceResult<T> = lock.withLock {
        val provider = context.serviceProviders.findForName(name)
            ?: return NO_PROVIDER.toResult("No service or factories found for service name '$name'")
        return tryGetService(provider, requiredType)
    }

    override fun <T : Any> tryGetService(clazz: KClass<T>): ServiceResult<T> = lock.withLock {
        val providers = context.serviceProviders.findAllForType(clazz)
        if (providers.isEmpty())
            return NO_PROVIDER.toResult("No service or factories found for type ${clazz.simpleNestedName}")

        val errors: MutableList<ServiceError> = arrayListOf()
        providers.forEach { provider ->
            tryGetService(provider, clazz)
                .onService { return this }
                .onError { errors += it }
        }

        return ServiceResult.fail(errors)
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <T : Any> tryGetService(provider: ServiceProvider, requiredType: KClass<T>): ServiceResult<T> {
        val instance = provider.instance as T?
        if (instance != null) {
            return ServiceResult.pass(instance)
        }

        val serviceError = canCreateService(provider)
        if (serviceError != null)
            return ServiceResult.fail(serviceError)

        return createService(provider, requiredType)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> createService(provider: ServiceProvider, requiredType: KClass<T>): ServiceResult<T> = lock.withLock {
        try {
            return serviceCreationStack.withServiceCreateKey(provider) {
                //Don't measure time globally, we need to not take into account the time to make dependencies
                val (anyResult, duration) = provider.createInstance(this)
                //Doesn't really matter, the object is not used anyway
                val result: ServiceResult<T> = anyResult as ServiceResult<T>
                if (result.serviceError != null)
                    return result

                val instance = result.getOrThrow()
                if (!requiredType.isInstance(instance))
                    return INVALID_TYPE.toResult(
                        errorMessage = "A service was found but type is incorrect, " +
                                "requested: ${requiredType.simpleNestedName}, actual: ${instance::class.simpleNestedName}",
                        extraMessage = "provider: ${provider.providerKey}"
                    )

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
                val serviceResult = tryGetService(it, clazz)
                serviceResult.serviceError?.let { serviceError ->
                    val warnMessage = "Could not create interfaced service ${clazz.simpleNestedName} with implementation ${it.primaryType.simpleNestedName} (from ${it.providerKey}):\n${serviceError.toSimpleString()}"
                    if (interfacedServiceErrors.add(warnMessage)) {
                        logger.debug(warnMessage)
                    }
                }
                serviceResult.getOrNull()
            }
    }

    override fun <T : Any> putServiceAs(t: T, clazz: KClass<out T>, name: String?) {
        context.serviceProviders.putServiceProvider(ClassServiceProvider(clazz, t))
    }

    override fun canCreateService(clazz: KClass<*>): ServiceError? {
        val providers = context.serviceProviders.findAllForType(clazz)
        if (providers.isEmpty())
            return NO_PROVIDER.toError("No service or factories found for type ${clazz.simpleNestedName}")

        val errors: MutableList<ServiceError> = arrayListOf()
        providers.forEach { provider ->
            when (val serviceError = canCreateService(provider)) {
                null -> return null
                else -> errors += serviceError
            }
        }

        return ServiceError.fromErrors(errors)
    }

    private fun canCreateService(provider: ServiceProvider): ServiceError? {
        if (provider.instance != null) return null

        return serviceCreationStack.withServiceCheckKey(provider) {
            provider.canInstantiate(this)
        }
    }

    private fun getLoadableService(requestedServiceStart: ServiceStart) = context.instantiableServiceAnnotationsMap
        .getAllInstantiableClasses()
        .filter { kClass ->
            val clazzServiceStart = kClass.findAnnotation<BService>()?.start ?: ServiceStart.DEFAULT
            return@filter requestedServiceStart == clazzServiceStart
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
