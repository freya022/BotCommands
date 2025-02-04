package io.github.freya022.botcommands.internal.core.service.provider

import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.ServiceError
import io.github.freya022.botcommands.api.core.service.ServiceError.ErrorType
import io.github.freya022.botcommands.api.core.service.annotations.*
import io.github.freya022.botcommands.api.core.utils.*
import io.github.freya022.botcommands.internal.core.exceptions.ServiceException
import io.github.freya022.botcommands.internal.core.service.DefaultServiceContainerImpl
import io.github.freya022.botcommands.internal.core.service.Singletons
import io.github.freya022.botcommands.internal.core.service.canCreateWrappedService
import io.github.freya022.botcommands.internal.core.service.tryGetWrappedService
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.resolveBestReference
import io.github.freya022.botcommands.internal.utils.throwArgument
import kotlin.collections.set
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.jvm.jvmErasure
import kotlin.time.Duration
import kotlin.time.measureTimedValue

/**
 * Either a class nested simple name, or a function signature for factories
 */
internal typealias ProviderName = String

internal typealias Instance = Any

// Don't use TimedValue as it is nullable
internal data class TimedInstantiation<R : Instance>(val instance: R, val duration: Duration)

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

    fun createInstance(serviceContainer: DefaultServiceContainerImpl): TimedInstantiation<*>

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

internal fun ServiceProvider.hasAnnotation(annotationType: KClass<out Annotation>): Boolean =
    annotations.any { it.annotationClass == annotationType }
internal inline fun <reified A : Annotation> ServiceProvider.hasAnnotation(): Boolean =
    hasAnnotation(A::class)

@Suppress("UNCHECKED_CAST")
internal fun <A : Annotation> ServiceProvider.findAnnotation(annotationType: KClass<A>): A? =
    annotations.firstOrNull { it.annotationClass == annotationType } as A?
internal inline fun <reified A : Annotation> ServiceProvider.findAnnotation(): A? =
    findAnnotation(A::class)

internal fun <A : Annotation> ServiceProvider.findAnnotations(annotationType: KClass<A>): List<A> =
    annotations.filterIsInstance(annotationType.java)
internal inline fun <reified A : Annotation> ServiceProvider.findAnnotations(): List<A> =
    annotations.filterIsInstance(A::class.java)

internal fun ServiceProvider.getProviderFunctionOrSignature(): Any = getProviderFunction() ?: getProviderSignature()

internal fun ServiceProvider.getAnnotatedServiceName(): String? {
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

internal fun ServiceProvider.getAnnotatedServicePriority(): Int {
    findAnnotation<ServicePriority>()?.let {
        return it.value
    }

    findAnnotation<BService>()?.let {
        return it.priority
    }

    // In case another annotation is used
    return 0
}

internal fun ServiceProvider.getServiceTypes(primaryType: KClass<*>): Set<KClass<*>> {
    val explicitTypes = findAnnotations<ServiceType>().flatMapTo(hashSetOf()) { it.types }
    val ignoredTypes = findAnnotations<IgnoreServiceTypes>().flatMapTo(hashSetOf()) { it.types }
    val interfacedServiceTypes = primaryType.java.allSuperclassesAndInterfaces
        .filter { it != primaryType.java && it.isAnnotationPresent(InterfacedService::class.java) }
        .mapTo(hashSetOf()) { it.kotlin }
    val additionalTypes = interfacedServiceTypes + explicitTypes - ignoredTypes

    val effectiveTypes = when {
        additionalTypes.isEmpty() -> setOf(primaryType)
        else -> buildSet(additionalTypes.size + 1) {
            this += primaryType
            this += additionalTypes.onEach {
                require(it.isAssignableFrom(primaryType)) {
                    "${it.simpleNestedName} is not a supertype of service ${primaryType.simpleNestedName}"
                }
            }
        }
    }

    return effectiveTypes
}

internal fun ServiceProvider.commonCanInstantiate(serviceContainer: DefaultServiceContainerImpl, annotatedElement: KAnnotatedElement, checkedClass: KClass<*>): ServiceError? {
    findAnnotations<Dependencies>().forEach { dependencies ->
        dependencies.value.forEach { dependency ->
            serviceContainer.canCreateService(dependency)?.let { serviceError ->
                return ErrorType.UNAVAILABLE_DEPENDENCY.toError("Conditional service '${primaryType.simpleNestedName}' depends on ${dependency.simpleNestedName} but it is not available", nestedError = serviceError)
            }
        }
    }

    // Services can be conditional
    findAnnotations<ConditionalService>().forEach { conditionalService ->
        conditionalService.checks.forEach {
            val instance = Singletons[it]

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

    annotatedElement.findAllAnnotationsWith<Condition>().forEach { (userCondition, metadataAnnotation) ->
        val checkerType = metadataAnnotation.type
        @Suppress("UNCHECKED_CAST")
        val checker = Singletons[checkerType] as CustomConditionChecker<Annotation>

        // Check the checker processes the annotation we just found
        @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
        val expectedCondition = (userCondition as java.lang.annotation.Annotation).annotationType()
        val actualCondition = checker.annotationType
        require(expectedCondition == actualCondition) {
            val conditionName = expectedCondition.simpleNestedName
            val checkerName = checkerType.simpleNestedName

            val requiredSuperclassName = CustomConditionChecker::class.simpleNestedName
            val requiredCheckerTypeArgument = expectedCondition.simpleNestedName

            "Custom condition checker $checkerName must implement $requiredSuperclassName<$requiredCheckerTypeArgument> to be usable in @$conditionName"
        }

        fun createError(errorMessage: String, nestedError: ServiceError? = null): ServiceError {
            val errorType = if (metadataAnnotation.fail) {
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
            checker.checkServiceAvailability(serviceContainer, checkedClass.java, userCondition)
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

internal inline fun <T : Any> measureTimedInstantiation(block: () -> T): TimedInstantiation<T> {
    val (value, duration) = measureTimedValue(block)
    return TimedInstantiation(value, duration)
}

internal inline fun <T : Any> measureNullableTimedInstantiation(block: () -> T?): TimedInstantiation<T>? {
    val (value, duration) = measureTimedValue(block)
    if (value == null) return null
    return TimedInstantiation(value, duration)
}

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

internal fun KFunction<*>.callConstructingFunction(serviceContainer: DefaultServiceContainerImpl): TimedInstantiation<*> {
    val params: MutableMap<KParameter, Any?> = hashMapOf()
    this.nonInstanceParameters.forEach {
        //Try to get a dependency, if it doesn't work and parameter isn't nullable / cannot be omitted, then return the message
        val dependencyResult = serviceContainer.tryGetWrappedService(it)
        params[it] = dependencyResult.service ?: when {
            it.type.isMarkedNullable -> null
            it.isOptional -> return@forEach
            else -> throw ServiceException(ErrorType.UNAVAILABLE_PARAMETER.toError(
                "Cannot get service for parameter '${it.bestName}' (${it.type.jvmErasure.simpleNestedName})",
                failedFunction = this,
                nestedError = dependencyResult.serviceError
            ))
        }
    }

    return measureTimedInstantiation {
        this.callStatic(serviceContainer, params)
            ?: throw ServiceException(ErrorType.PROVIDER_RETURNED_NULL.toError(
                errorMessage = "Service factory returned null",
                failedFunction = this
            ))
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