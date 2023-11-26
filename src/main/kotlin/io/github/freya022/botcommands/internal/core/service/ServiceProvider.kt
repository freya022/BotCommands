package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.ServiceError
import io.github.freya022.botcommands.api.core.service.ServiceError.ErrorType
import io.github.freya022.botcommands.api.core.service.ServiceResult
import io.github.freya022.botcommands.api.core.service.annotations.*
import io.github.freya022.botcommands.api.core.utils.bestName
import io.github.freya022.botcommands.api.core.utils.isAssignableFrom
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.utils.*
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.resolveReference
import io.github.oshai.kotlinlogging.KotlinLogging
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
    val priority: Int

    val instance: Any?

    fun canInstantiate(serviceContainer: ServiceContainerImpl): ServiceError?

    fun createInstance(serviceContainer: ServiceContainerImpl): TimedInstantiation

    override fun compareTo(other: ServiceProvider): Int {
        // Reverse order
        val priorityCmp = priority.compareTo(other.priority)
        if (priorityCmp != 0) return -priorityCmp

        // Prioritize service coming from service factories
        if (this is FunctionServiceProvider && other !is FunctionServiceProvider) {
            return -1
        } else if (other is FunctionServiceProvider && this !is FunctionServiceProvider) {
            return 1
        }

        return name.compareTo(other.name)
    }
}

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

internal fun KAnnotatedElement.getServiceTypes(returnType: KClass<*>): Set<KClass<*>> {
    val explicitTypes = when (val serviceType = findAnnotation<ServiceType>()) {
        null -> setOf(returnType)
        else -> buildSet(serviceType.types.size + 1) {
            this += returnType
            this += serviceType.types.onEach {
                if (!it.isAssignableFrom(returnType)) {
                    throw IllegalArgumentException("${it.simpleNestedName} is not a supertype of service ${returnType.simpleNestedName}")
                }
            }
        }
    }

    val interfacedServiceTypes = returnType.allSuperclasses.filter { it.hasAnnotation<InterfacedService>() }
    val existingServiceTypes = interfacedServiceTypes.intersect(explicitTypes)
    if (existingServiceTypes.isNotEmpty()) {
        logger.warn { "Instance of ${returnType.simpleNestedName} should not have their implemented interfaced services (${existingServiceTypes.joinToString { it.simpleNestedName }}) in ${annotationRef<ServiceType>()}, source: $this" }
    }

    return explicitTypes + interfacedServiceTypes
}

internal fun KAnnotatedElement.commonCanInstantiate(serviceContainer: ServiceContainerImpl, checkedClass: KClass<*>): ServiceError? {
    findAnnotation<Dependencies>()?.value?.let { dependencies ->
        dependencies.forEach { dependency ->
            serviceContainer.canCreateService(dependency)?.let { serviceError ->
                return ErrorType.UNAVAILABLE_DEPENDENCY.toError("Conditional service depends on ${dependency.simpleNestedName} but it is not available", nestedError = serviceError)
            }
        }
    }

    // Services can be conditional
    findAnnotation<ConditionalService>()?.let { conditionalService ->
        conditionalService.checks.forEach {
            val instance = it.createSingleton()
            instance.checkServiceAvailability(serviceContainer.context, checkedClass.java)
                ?.let { errorMessage ->
                    return ErrorType.FAILED_CONDITION.toError(
                        errorMessage,
                        // instance::checkServiceAvailability does not bind to the actual instance
                        failedFunction = instance::checkServiceAvailability.resolveReference(instance::class)
                    )
                }
        }
    }

    serviceContainer.context.customConditionsContainer.customConditionCheckers.forEach { customCondition ->
        val annotation = customCondition.getCondition(this)
        if (annotation != null) {
            val checker = customCondition.checker
            checker.checkServiceAvailability(serviceContainer.context, checkedClass.java, annotation)
                ?.let { errorMessage ->
                    val errorType = if (customCondition.conditionMetadata.fail) {
                        ErrorType.FAILED_FATAL_CUSTOM_CONDITION
                    } else {
                        ErrorType.FAILED_CUSTOM_CONDITION
                    }

                    return errorType.toError(
                        errorMessage,
                        // instance::checkServiceAvailability does not bind to the actual instance
                        failedFunction = checker::checkServiceAvailability.resolveReference(checker::class)
                    )
                }
        }
    }

    //All checks passed, return no error message
    return null
}

internal inline fun <T> measureTimedInstantiation(block: () -> T): TimedInstantiation {
    val measureTimedValue = measureTimedValue(block)
    return TimedInstantiation(ServiceResult.pass(measureTimedValue.value!!), measureTimedValue.duration)
}

internal fun ServiceResult<*>.toFailedTimedInstantiation(): TimedInstantiation {
    if (serviceError != null) {
        return TimedInstantiation(this, Duration.INFINITE)
    } else {
        throwInternal("Cannot use ${::toFailedTimedInstantiation.shortSignatureNoSrc} if service got created (${getOrThrow()::class.simpleNestedName}")
    }
}

internal fun KFunction<*>.checkConstructingFunction(serviceContainer: ServiceContainerImpl): ServiceError? {
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

/**
 * NOTE: Lazy services do not get checked if they can be instantiated,
 * this aligns with the behavior of a user using `ServiceContainer.lazy`.
 */
internal fun ServiceContainer.canCreateWrappedService(parameter: KParameter): ServiceError? {
    val type = parameter.type
    val name = parameter.findAnnotation<ServiceName>()?.value
    return if (name != null) {
        when (type.jvmErasure) {
            Lazy::class -> null //Lazy exception
            List::class -> null //Might be empty if no service were available, which is ok
            else -> canCreateService(type.jvmErasure) //TODO support name
        }
    } else {
        when (type.jvmErasure) {
            Lazy::class -> null //Lazy exception
            List::class -> null //Might be empty if no service were available, which is ok
            else -> canCreateService(type.jvmErasure)
        }
    }
}

internal fun KFunction<*>.callConstructingFunction(serviceContainer: ServiceContainerImpl): TimedInstantiation {
    val params: MutableMap<KParameter, Any?> = hashMapOf()
    this.nonInstanceParameters.forEach {
        //Try to get a dependency, if it doesn't work and parameter isn't nullable / cannot be omitted, then return the message
        val dependencyResult = serviceContainer.tryGetWrappedService(it)
        params[it] = dependencyResult.service ?: when {
            it.type.isMarkedNullable -> null
            it.isOptional -> return@forEach
            else -> return ErrorType.UNAVAILABLE_PARAMETER.toResult<Any>(
                "Cannot get service for parameter '${it.bestName}' (${it.type.jvmErasure.simpleNestedName})",
                failedFunction = this,
                nestedError = dependencyResult.serviceError
            ).toFailedTimedInstantiation()
        }
    }

    return measureTimedInstantiation { this.callStatic(serviceContainer, params) }
}

internal fun <R> KFunction<R>.callStatic(serviceContainer: ServiceContainerImpl, args: MutableMap<KParameter, Any?>): R {
    if (this.isSuspend) {
        throwUser(this, "Suspending functions are not supported in this context")
    }

    return when (val instanceParameter = this.instanceParameter) {
        null -> this.callBy(args)
        else -> {
            val instanceErasure = instanceParameter.type.jvmErasure
            val instance = instanceErasure.objectInstance
                ?: serviceContainer.tryGetService(instanceErasure).getOrThrow { (_, errorMessage) ->
                    throwUser(this, "Could not run function as it is not static, the declaring class isn't an object, and service creation failed: $errorMessage")
                }
            args[instanceParameter] = instance

            this.callBy(args)
        }
    }
}