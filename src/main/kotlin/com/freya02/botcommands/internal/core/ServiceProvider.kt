package com.freya02.botcommands.internal.core

import com.freya02.botcommands.api.core.ServiceResult
import com.freya02.botcommands.api.core.annotations.ConditionalService
import com.freya02.botcommands.api.core.annotations.Dependencies
import com.freya02.botcommands.api.core.annotations.ServiceType
import com.freya02.botcommands.internal.simpleNestedName
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.utils.ReflectionUtils.shortSignatureNoSrc
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.jvm.jvmErasure
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimedValue

/**
 * Either a class nested simple name, or a function signature for factories
 */
internal typealias ProviderName = String

internal interface ServiceProvider {
    val name: String
    val providerKey: ProviderName
    val primaryType: KClass<*>
    val types: List<KClass<*>>

    val instance: Any?

    fun canInstantiate(serviceContainer: ServiceContainerImpl): String?

    fun createInstance(serviceContainer: ServiceContainerImpl): ServiceContainerImpl.TimedInstantiation
}

internal fun KAnnotatedElement.getServiceTypes(returnType: KClass<*>) = when (val serviceType = findAnnotation<ServiceType>()) {
    null -> listOf(returnType)
    else -> buildList(serviceType.types.size + 1) {
        this += returnType
        this += serviceType.types.onEach {
            if (!it.isSuperclassOf(returnType)) {
                throw IllegalArgumentException("${it.simpleNestedName} is not a supertype of service ${returnType.simpleNestedName}")
            }
        }
    }
}

internal fun KAnnotatedElement.commonCanInstantiate(serviceContainer: ServiceContainerImpl): String? {
    findAnnotation<Dependencies>()?.value?.let { dependencies ->
        dependencies.forEach { dependency ->
            serviceContainer.canCreateService(dependency)?.let { errorMessage ->
                return "Conditional service depends on ${dependency.simpleNestedName} but it is not available: $errorMessage"
            }
        }
    }

    // Services can be conditional
    findAnnotation<ConditionalService>()?.let { conditionalService ->
        conditionalService.checks.forEach {
            val instance = it.objectInstance ?: it.createInstance()
            instance.checkServiceAvailability(serviceContainer.context)
                ?.let { errorMessage -> return errorMessage }
        }
    }

    //All checks passed, return no error message
    return null
}

@OptIn(ExperimentalTime::class)
internal fun <T> TimedValue<T>.toTimedInstantiationOrNull() =
    this.value?.let { ServiceContainerImpl.TimedInstantiation(ServiceResult.pass(it), this.duration) }

@OptIn(ExperimentalTime::class)
internal fun <T> TimedValue<T>.toTimedInstantiation() =
    ServiceContainerImpl.TimedInstantiation(ServiceResult.pass(this.value!!), this.duration)

internal fun ServiceResult<*>.toFailedTimedInstantiation(): ServiceContainerImpl.TimedInstantiation {
    if (errorMessage != null) {
        return ServiceContainerImpl.TimedInstantiation(ServiceResult.fail<Any>(errorMessage), Duration.INFINITE)
    } else {
        throwInternal("Cannot use ${::toFailedTimedInstantiation.shortSignatureNoSrc} if service got created (${getOrThrow()::class.simpleNestedName}")
    }
}

internal fun <R> KFunction<R>.callStatic(vararg args: Any?): R {
    return when (val instanceParameter = this.instanceParameter) {
        null -> this.call(*args)
        else -> {
            val companionObjectClazz = instanceParameter.type.jvmErasure
            if (!companionObjectClazz.isCompanion)
                throwInternal("Tried to call a non-static function but the ${companionObjectClazz.simpleNestedName} instance parameter is not a companion object")
            val companionObjectInstance = companionObjectClazz.objectInstance
                ?: throwInternal("Tried to call a non-static function but the ${companionObjectClazz.simpleNestedName} instance parameter is not a companion object")

            this.call(companionObjectInstance, *args)
        }
    }
}