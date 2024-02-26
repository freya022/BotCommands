package io.github.freya022.botcommands.internal.core.service

import io.github.classgraph.ClassInfo
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.service.ClassGraphProcessor
import io.github.freya022.botcommands.api.core.service.ServiceError.ErrorType.*
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.service.provider.ServiceProvider
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.freya022.botcommands.internal.utils.throwUser
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmName

@BService(priority = Int.MAX_VALUE)
internal class InstantiableServiceAnnotationsMap internal constructor(private val context: BContextImpl) {
    private class InterfacedType(val clazz: KClass<*>, annotation: InterfacedService) {
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

    //Annotation type match such as: Map<KClass<A>, Map<KClass<*>, A>>
    private val map: Map<KClass<out Annotation>, Set<KClass<*>>> = context.serviceAnnotationsMap
        .map
        //Filter out non-instantiable classes
        .mapValues { (_, map) ->
            map.filterTo(hashSetOf()) { clazz ->
                // We still need to check every provider ourselves
                context.serviceProviders.findAllForType(clazz).any { provider ->
                    val serviceError = context.serviceContainer.canCreateService(provider) ?: return@any true

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

                            else -> true
                        }
                    } else {
                        when (serviceError.errorType) {
                            UNKNOWN, NO_USABLE_PROVIDER, PROVIDER_RETURNED_NULL, NO_PROVIDER, NON_UNIQUE_PROVIDERS -> throwInternal(serviceError.errorMessage)

                            UNAVAILABLE_PARAMETER, UNAVAILABLE_INJECTED_SERVICE, DYNAMIC_NOT_INSTANTIABLE, INVALID_CONSTRUCTING_FUNCTION, INVALID_TYPE, FAILED_FATAL_CUSTOM_CONDITION ->
                                throwUser("Could not load service provider '${provider.name}':\n${serviceError.toDetailedString()}")

                            UNAVAILABLE_DEPENDENCY, FAILED_CONDITION, FAILED_CUSTOM_CONDITION -> {
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
                                false
                            }
                        }
                    }
                }
            }
        }

    init {
        val typeToImplementations = hashMapOf<InterfacedType, MutableSet<ServiceProvider>>()
        getAllInstantiableClasses().forEach { kClass ->
            // For each service, take their implemented interfaced services
            // and put them in a map as to figure out if multiple - instantiable - implementation exists
            val provider = context.serviceProviders.findForType(kClass)
                ?: throwInternal("Could not find back service provider for ${kClass.simpleNestedName}")
            val interfacedTypes = provider.types.mapNotNull { clazz ->
                clazz.findAnnotation<InterfacedService>()?.let { InterfacedType(clazz, it) }
            }
            //Only check those implementing an interfaced service
            if (interfacedTypes.isEmpty()) return@forEach
            if (provider.canInstantiate(context.serviceContainer) != null) return@forEach

            interfacedTypes.forEach { interfacedType ->
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
                appendLine("Interfaced services with 'acceptMultiple = false' cannot have multiple implementations, " +
                        "please adjust your services so at most one implementation is instantiable:")

                nonUniqueImplementations.forEach { (interfacedServiceType, implementations) ->
                    appendLine("${interfacedServiceType.clazz.simpleNestedName}:\n${implementations.joinAsList { it.providerKey }}")
                }
            }
            throw IllegalStateException(message)
        }

        typeToImplementations.forEach { (interfacedType, providers) ->
            logger.trace { "Found implementations of ${interfacedType.clazz.simpleNestedName} in ${providers.joinToString { it.primaryType.simpleNestedName }}" }
        }
    }

    internal inline fun <reified A : Annotation> get(): Set<KClass<*>>? = map[A::class]

    internal fun getAllInstantiableClasses() = map.flatMap { (_, annotationReceiversMap) -> annotationReceiversMap }
}

private val logger = KotlinLogging.logger { }

internal class ServiceAnnotationsMap internal constructor() {
    // Annotation type => Classes with said annotation
    private val _map: MutableMap<KClass<out Annotation>, MutableSet<KClass<*>>> = hashMapOf()

    internal val map: Map<KClass<out Annotation>, Set<KClass<*>>>
        get() = _map

    internal fun <A : Annotation> put(annotationReceiver: KClass<*>, annotationType: KClass<A>) {
        val annotatedClasses = _map.computeIfAbsent(annotationType) { hashSetOf() }
        if (!annotatedClasses.add(annotationReceiver))
            return logger.warn { "An annotation instance of type '${annotationType.simpleNestedName}' already exists on class '${annotationReceiver.simpleNestedName}'" }
    }
}

internal class ServiceAnnotationsMapProcessor internal constructor(
    private val config: BConfig,
    private val serviceAnnotationsMap: ServiceAnnotationsMap
) : ClassGraphProcessor {
    override fun processClass(context: BContext, classInfo: ClassInfo, kClass: KClass<*>, isService: Boolean) {
        //Fill map with all the @Command, @Resolver, etc... declarations
        if (isService) {
            classInfo.annotationInfo.forEach { annotationInfo ->
                if (config.serviceConfig.serviceAnnotations.any { it.jvmName == annotationInfo.name }) {
                    serviceAnnotationsMap.put(
                        annotationReceiver = kClass,
                        annotationType = annotationInfo.classInfo.loadClass(Annotation::class.java).kotlin
                    )
                }
            }
        }
    }
}