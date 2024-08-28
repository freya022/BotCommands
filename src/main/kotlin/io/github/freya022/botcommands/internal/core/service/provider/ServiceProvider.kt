package io.github.freya022.botcommands.internal.core.service.provider

import io.github.freya022.botcommands.api.core.service.LazyService
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.ServiceError
import io.github.freya022.botcommands.api.core.service.ServiceError.ErrorType
import io.github.freya022.botcommands.api.core.service.ServiceResult
import io.github.freya022.botcommands.api.core.service.annotations.*
import io.github.freya022.botcommands.api.core.utils.bestName
import io.github.freya022.botcommands.api.core.utils.isAssignableFrom
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.core.exceptions.ServiceException
import io.github.freya022.botcommands.internal.core.service.DefaultServiceContainerImpl
import io.github.freya022.botcommands.internal.core.service.getLazyElementErasure
import io.github.freya022.botcommands.internal.core.service.tryGetWrappedService
import io.github.freya022.botcommands.internal.utils.*
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.function
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.resolveBestReference
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.collections.set
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.jvm.jvmErasure
import kotlin.time.Duration
import kotlin.time.measureTimedValue
import kotlin.Lazy as KotlinLazy

private val logger = KotlinLogging.logger { }

/**
 * Either a class nested simple name, or a function signature for factories
 */
internal typealias ProviderName = String

internal data class TimedInstantiation(val result: ServiceResult<*>, val duration: Duration)

internal sealed interface ServiceProvider : Comparable<ServiceProvider> {
    val name: String
    val providerKey: ProviderName
    val primaryType: KClass<*>
    val types: Set<KClass<*>>
    val isPrimary: Boolean
    val isLazy: Boolean
    val priority: Int
    val annotations: Collection<Annotation>

    val instance: Any?

    fun canInstantiate(serviceContainer: DefaultServiceContainerImpl): ServiceError?

    fun createInstance(serviceContainer: DefaultServiceContainerImpl): TimedInstantiation

    fun getProviderFunction(): KFunction<*>?

    fun getProviderSignature(): String

    override fun compareTo(other: ServiceProvider): Int {
        val priorityCmp = other.priority.compareTo(priority) // Reverse order
        if (priorityCmp != 0) return priorityCmp

        // Prioritize service coming from service factories
        if (this is FunctionServiceProvider && other !is FunctionServiceProvider) {
            return -1
        } else if (other is FunctionServiceProvider && this !is FunctionServiceProvider) {
            return 1
        }

        val nameCmp = name.compareTo(other.name)
        if (nameCmp != 0) return nameCmp

        return providerKey.compareTo(other.providerKey)
    }

    companion object {
        internal val nullServiceError = ErrorType.UNKNOWN.toError("Returning a sentinel service error is impossible")
    }
}

internal fun ServiceProvider.getProviderFunctionOrSignature(): Any = getProviderFunction() ?: getProviderSignature()

internal fun KAnnotatedElement.getAnnotatedServiceName(): String? {
    findAnnotation<ServiceName>()?.let {
        if (it.value.isNotBlank()) {
            return it.value
        }
    }

    findAnnotation<BService>()?.let {
        if (it.name.isNotBlank()) {
            return it.name
        }
    }

    return null
}

internal fun KAnnotatedElement.getAnnotatedServicePriority(): Int {
    findAnnotation<ServicePriority>()?.let {
        return it.value
    }

    findAnnotation<BService>()?.let {
        return it.priority
    }

    // In case another annotation is used
    return 0
}

internal fun KAnnotatedElement.getServiceTypes(primaryType: KClass<*>): Set<KClass<*>> {
    val explicitTypes = when (val serviceType = findAnnotation<ServiceType>()) {
        null -> setOf(primaryType)
        else -> buildSet(serviceType.types.size + 1) {
            this += primaryType
            this += serviceType.types.onEach {
                require(it.isAssignableFrom(primaryType)) {
                    "${it.simpleNestedName} is not a supertype of service ${primaryType.simpleNestedName}"
                }
            }
        }
    }
    val removedTypes = findAnnotation<IgnoreServiceTypes>()?.types?.toSet() ?: emptySet()

    val interfacedServiceTypes = primaryType.allSuperclasses.filter { it.hasAnnotation<InterfacedService>() }
    val existingServiceTypes = interfacedServiceTypes.intersect(explicitTypes) - removedTypes
    if (existingServiceTypes.isNotEmpty()) {
        logger.warn { "Instance of ${primaryType.simpleNestedName} should not have their implemented interfaced services (${existingServiceTypes.joinToString { it.simpleNestedName }}) in ${annotationRef<ServiceType>()}, source: $this" }
    }

    return (explicitTypes + interfacedServiceTypes) - removedTypes
}

context(ServiceProvider)
internal fun KAnnotatedElement.commonCanInstantiate(serviceContainer: DefaultServiceContainerImpl, checkedClass: KClass<*>): ServiceError? {
    findAnnotation<Dependencies>()?.value?.let { dependencies ->
        dependencies.forEach { dependency ->
            serviceContainer.canCreateService(dependency)?.let { serviceError ->
                return ErrorType.UNAVAILABLE_DEPENDENCY.toError("Conditional service '${primaryType.simpleNestedName}' depends on ${dependency.simpleNestedName} but it is not available", nestedError = serviceError)
            }
        }
    }

    // Services can be conditional
    findAnnotation<ConditionalService>()?.let { conditionalService ->
        conditionalService.checks.forEach {
            val instance = it.createSingleton()

            fun createError(errorMessage: String, nestedError: ServiceError? = null): ServiceError {
                return ErrorType.FAILED_CONDITION.toError(
                    errorMessage,
                    nestedError = nestedError,
                    // instance::checkServiceAvailability does not bind to the actual instance
                    extra = mapOf(
                        "Failed check" to instance::checkServiceAvailability.resolveBestReference(),
                        "For" to getProviderFunctionOrSignature()
                    )
                )
            }

            val errorMessage = try {
                instance.checkServiceAvailability(serviceContainer, checkedClass.java)
            } catch (e: ServiceException) {
                val error = e.serviceError
                return createError("A service required by the condition checker is missing", nestedError = error)
            }

            if (errorMessage == null) return@forEach

            return createError(errorMessage)
        }
    }

    serviceContainer.serviceBootstrap.customConditionsContainer.customConditionCheckers.forEach { customCondition ->
        val annotation = customCondition.getCondition(this) ?: return@forEach
        val checker = customCondition.checker

        fun createError(errorMessage: String, nestedError: ServiceError? = null): ServiceError {
            val errorType = if (customCondition.conditionMetadata.fail) {
                ErrorType.FAILED_FATAL_CUSTOM_CONDITION
            } else {
                ErrorType.FAILED_CUSTOM_CONDITION
            }
            return errorType.toError(
                errorMessage,
                nestedError = nestedError,
                // checker::checkServiceAvailability does not bind to the actual instance
                extra = mapOf(
                    "Failed check" to checker::checkServiceAvailability.resolveBestReference(),
                    "For" to getProviderFunctionOrSignature()
                )
            )
        }

        val errorMessage = try {
            checker.checkServiceAvailability(serviceContainer, checkedClass.java, annotation)
        } catch (e: ServiceException) {
            val error = e.serviceError
            return createError("A service required by the condition checker is missing", nestedError = error)
        }

        if (errorMessage == null) return@forEach

        return createError(errorMessage)
    }

    //All checks passed, return no error message
    return null
}

internal inline fun <T : Any> measureTimedInstantiation(block: () -> T): TimedInstantiation {
    val (value, duration) = measureTimedValue(block)
    return TimedInstantiation(ServiceResult.pass(value), duration)
}

internal inline fun <T : Any?> measureNullableTimedInstantiation(block: () -> T): TimedInstantiation? {
    val (value, duration) = measureTimedValue(block)
    if (value == null) return null
    return TimedInstantiation(ServiceResult.pass(value), duration)
}

internal fun ServiceError.toFailedTimedInstantiation(): TimedInstantiation =
    TimedInstantiation(ServiceResult.fail<Any>(this), Duration.INFINITE)

internal fun KFunction<*>.checkConstructingFunction(serviceContainer: DefaultServiceContainerImpl): ServiceError? {
    this.nonInstanceParameters.forEach {
        serviceContainer.canCreateWrappedService(it)?.let { serviceError ->
            when {
                it.type.isMarkedNullable -> return@forEach //Ignore
                it.isOptional -> return@forEach //Ignore
                else -> return ErrorType.UNAVAILABLE_PARAMETER.toError(
                    errorMessage = "Cannot get service for parameter '${it.bestName}' (${it.type.jvmErasure.simpleNestedName})",
                    failedFunction = this,
                    nestedError = serviceError
                )
            }
        }
    }

    return null
}

private val warnedLazy = hashSetOf<KParameter>()

/**
 * NOTE: Lazy services do not get checked if they can be instantiated,
 * this aligns with the behavior of a user using `ServiceContainer.lazy`.
 */
internal fun ServiceContainer.canCreateWrappedService(parameter: KParameter): ServiceError? {
    val type = parameter.type
    //TODO remove
    if (type.jvmErasure == KotlinLazy::class) {
        if (warnedLazy.add(parameter)) {
            logger.warn {
                buildString {
                    val (_, isNullable) = getLazyElementErasure(parameter)
                    if (isNullable) {
                        appendLine("Injection of nullable kotlin.Lazy will be removed in a future release, consider replacing your Lazy#value/Lazy#getValue call with ServiceContainer#getServiceOrNull")
                    } else {
                        appendLine("Injection of kotlin.Lazy will be removed in a future release and has been replaced with ${classRef<LazyService<*>>()}")
                    }
                    appendLine("Parameter: ${parameter.bestName} (#${parameter.index})")
                    appendLine("Function: ${parameter.function.shortSignature}")
                }
            }
        }
    }

    if (type.jvmErasure == KotlinLazy::class || type.jvmErasure == LazyService::class) {
        return null //Lazy exception
    } else if (type.jvmErasure == List::class) {
        return null //Might be empty if no service were available, which is ok
    }

    val requestedMandatoryName = parameter.findAnnotation<ServiceName>()?.value
    return if (requestedMandatoryName != null) {
        canCreateService(requestedMandatoryName, type.jvmErasure)
    } else {
        val serviceErrorByParameterName = parameter.name?.let { parameterName ->
            canCreateService(parameterName, type.jvmErasure)
        }

        // Try to get a service with the parameter name
        if (serviceErrorByParameterName == null)
            return null

        // If no service by parameter name was found, try by type
        canCreateService(type.jvmErasure)
    }
}

internal fun KFunction<*>.callConstructingFunction(serviceContainer: DefaultServiceContainerImpl): TimedInstantiation {
    val params: MutableMap<KParameter, Any?> = hashMapOf()
    this.nonInstanceParameters.forEach {
        //Try to get a dependency, if it doesn't work and parameter isn't nullable / cannot be omitted, then return the message
        val dependencyResult = serviceContainer.tryGetWrappedService(it)
        params[it] = dependencyResult.service ?: when {
            it.type.isMarkedNullable -> null
            it.isOptional -> return@forEach
            else -> return ErrorType.UNAVAILABLE_PARAMETER.toError(
                "Cannot get service for parameter '${it.bestName}' (${it.type.jvmErasure.simpleNestedName})",
                failedFunction = this,
                nestedError = dependencyResult.serviceError
            ).toFailedTimedInstantiation()
        }
    }

    return measureTimedInstantiation {
        this.callStatic(serviceContainer, params)
            ?: return ErrorType.PROVIDER_RETURNED_NULL.toError(
                errorMessage = "Service factory returned null",
                failedFunction = this
            ).toFailedTimedInstantiation()
    }
}

internal fun <R> KFunction<R>.callStatic(serviceContainer: DefaultServiceContainerImpl, args: MutableMap<KParameter, Any?>): R {
    if (this.isSuspend) {
        throwArgument(this, "Suspending functions are not supported in this context")
    }

    return when (val instanceParameter = this.instanceParameter) {
        null -> this.callBy(args)
        else -> {
            val instanceErasure = instanceParameter.type.jvmErasure
            val instance = instanceErasure.objectInstance
                ?: serviceContainer.tryGetService(instanceErasure).getOrThrow {
                    throwArgument(this, "Could not run function as it is not static, the declaring class isn't an object, and service creation failed:\n${it.toDetailedString()}")
                }
            args[instanceParameter] = instance

            this.callBy(args)
        }
    }
}