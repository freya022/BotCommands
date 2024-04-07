package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.service.ServiceError.ErrorType.*
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.core.service.provider.ServiceProvider
import io.github.freya022.botcommands.internal.core.service.provider.ServiceProviders
import io.github.freya022.botcommands.internal.utils.reference
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.freya022.botcommands.internal.utils.throwUser
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.findAnnotation

private val logger = KotlinLogging.logger { }

@InterfacedService(acceptMultiple = false)
internal interface InstantiableServices {
    val availablePrimaryTypes: Set<KClass<*>>

    val allAvailableTypes: Set<KClass<*>>
}

@Service
internal class SpringInstantiableServices internal constructor(
        config: BConfig,
        applicationContext: ApplicationContext
) : InstantiableServices {
    final override val availablePrimaryTypes: Set<KClass<*>>

    final override val allAvailableTypes: Set<KClass<*>>

    init {
        val allBeans = applicationContext.beanDefinitionNames
            .asSequence()
            .map { applicationContext.getType(it) }
            .filter { type ->
                if (config.packages.any { type.packageName.startsWith(it) })
                    return@filter true
                if (type in config.classes)
                    return@filter true
                return@filter false
            }
            .mapTo(hashSetOf()) { it.kotlin }
        availablePrimaryTypes = allBeans
        allAvailableTypes = allBeans + allBeans.flatMapTo(hashSetOf()) { it.allSuperclasses }
    }
}

@BService(priority = Int.MAX_VALUE)
@RequiresDefaultInjection
internal class DefaultInstantiableServices internal constructor(serviceProviders: ServiceProviders, serviceContainer: ServiceContainerImpl) : InstantiableServices {
    // TODO put in init block
    internal final val availableProviders: Set<ServiceProvider> = serviceProviders.allProviders.mapNotNullTo(sortedSetOf()) { provider ->
        val serviceError = serviceContainer.canCreateService(provider) ?: return@mapNotNullTo provider

        if (serviceError.errorType == UNAVAILABLE_PARAMETER) {
            if (serviceError.nestedError?.errorType == NON_UNIQUE_PROVIDERS) {
                throwUser("Could not load service provider '${provider.name}':\n${serviceError.toDetailedString()}")
            }
        }

        if (provider.isLazy) {
            when (serviceError.errorType) {
                UNKNOWN, NO_USABLE_PROVIDER, PROVIDER_RETURNED_NULL, NO_PROVIDER, NON_UNIQUE_PROVIDERS -> throwInternal(serviceError.errorMessage)

                /*UNAVAILABLE_PARAMETER, UNAVAILABLE_INJECTED_SERVICE, DYNAMIC_NOT_INSTANTIABLE,*/ INVALID_CONSTRUCTING_FUNCTION, INVALID_TYPE/*, FAILED_FATAL_CUSTOM_CONDITION*/ ->
                throwUser("Could not load lazy service provider '${provider.name}':\n${serviceError.toDetailedString()}")

                else -> provider
            }
        } else {
            when (serviceError.errorType) {
                UNKNOWN, NO_USABLE_PROVIDER, PROVIDER_RETURNED_NULL, NO_PROVIDER, NON_UNIQUE_PROVIDERS -> throwInternal(serviceError.errorMessage)

                UNAVAILABLE_PARAMETER, DYNAMIC_NOT_INSTANTIABLE, INVALID_CONSTRUCTING_FUNCTION, INVALID_TYPE, FAILED_FATAL_CUSTOM_CONDITION ->
                    throwUser("Could not load service provider '${provider.name}':\n${serviceError.toDetailedString()}")

                UNAVAILABLE_DEPENDENCY, FAILED_CONDITION, FAILED_CUSTOM_CONDITION, UNAVAILABLE_INSTANCE -> {
                    if (logger.isTraceEnabled()) {
                        logger.trace { "Service provider '${provider.name}' not loaded:\n${serviceError.toDetailedString()}" }
                    } else if (logger.isDebugEnabled()) {
                        logger.debug {
                            buildString {
                                append("Service provider '${provider.name}' not loaded")
                                serviceError.appendPostfixSimpleString()
                            }
                        }
                    }
                    null
                }
            }
        }
    }

    init {
        // Lazy providers are counted as available, and can be reported as duplicated, this is the correct behavior.
        val duplicatedNamedProviders = availableProviders
            .groupBy { it.name }
            .filter { it.value.size > 1 }

        check(duplicatedNamedProviders.isEmpty()) {
            buildString {
                appendLine("More than one service provider define usable services by the same name:")
                appendLine(duplicatedNamedProviders.entries.joinAsList("-") { (k, v) -> "$k\n${v.joinAsList(linePrefix = "    -") { it.getProviderSignature() }}" })
                appendLine()
                appendLine("Consider using a condition so only one of them can be used, or use a different name")
            }
        }
    }

    override val availablePrimaryTypes: Set<KClass<*>> = availableProviders.mapTo(hashSetOf()) { it.primaryType }

    override val allAvailableTypes: Set<KClass<*>> = availableProviders.flatMapTo(hashSetOf()) { it.types }

    init {
        class InterfacedType(val clazz: KClass<*>, annotation: InterfacedService) {
            val acceptMultiple = annotation.acceptMultiple

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as InterfacedType

                return clazz == other.clazz
            }

            override fun hashCode(): Int {
                return clazz.hashCode()
            }
        }

        val typeToImplementations = hashMapOf<InterfacedType, MutableSet<ServiceProvider>>()
        availableProviders.forEach { provider ->
            // For each service, take their implemented interfaced services
            // and put them in a map as to figure out if multiple - instantiable - implementation exists
            provider.types.forEach forEachType@{ clazz ->
                val annotation = clazz.findAnnotation<InterfacedService>() ?: return@forEachType
                val interfacedType = InterfacedType(clazz, annotation)

                typeToImplementations
                    .computeIfAbsent(interfacedType) { hashSetOf() }
                    .add(provider)
            }
        }

        val nonUniqueImplementations = typeToImplementations
            // Only keep single-implementation interfaced service which have multiple implementations
            .filter { (interfacedType, providers) -> !interfacedType.acceptMultiple && providers.size > 1 }
        if (nonUniqueImplementations.isNotEmpty()) {
            val message = buildString {
                appendLine("Interfaced services with '${InterfacedService::acceptMultiple.reference} = false' cannot have multiple implementations, " +
                        "please adjust your services so at most one implementation is instantiable:")

                nonUniqueImplementations.forEach { (interfacedServiceType, implementations) ->
                    appendLine("${interfacedServiceType.clazz.simpleNestedName}:\n${implementations.joinAsList { it.getProviderSignature() }}")
                }
            }
            throw IllegalStateException(message)
        }

        typeToImplementations.forEach { (interfacedType, providers) ->
            logger.trace { "Found implementations of ${interfacedType.clazz.simpleNestedName} in ${providers.joinToString { it.primaryType.simpleNestedName }}" }
        }
    }
}