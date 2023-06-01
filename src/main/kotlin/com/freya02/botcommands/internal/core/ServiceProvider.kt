package com.freya02.botcommands.internal.core

import com.freya02.botcommands.api.core.annotations.ConditionalService
import com.freya02.botcommands.api.core.annotations.Dependencies
import com.freya02.botcommands.api.core.annotations.ServiceType
import com.freya02.botcommands.internal.simpleNestedName
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSuperclassOf

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