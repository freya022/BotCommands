package io.github.freya022.botcommands.api.core.config

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.ServiceSupplier
import io.github.freya022.botcommands.api.core.service.ServiceSupplierBuilder
import io.github.freya022.botcommands.api.core.service.annotations.*
import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import io.github.freya022.botcommands.api.core.utils.toImmutableMap
import io.github.freya022.botcommands.api.core.utils.unmodifiableView
import io.github.freya022.botcommands.internal.core.config.ConfigDSL
import io.github.freya022.botcommands.internal.utils.putIfAbsentOrThrow
import kotlin.reflect.KClass

@InjectedService
interface BServiceConfig : IConfig, BServiceConfigProps {
    override val configType get() = BServiceConfig::class.java
}

interface BServiceConfigProps {
    /**
     * Enables debugging of service loading.
     *
     * This includes the operation type (check/create), the run time, the type of the service and where it comes from.
     */
    val debug: Boolean

    val serviceSuppliers: Map<KClass<*>, ServiceSupplier<*>>
}

@ConfigDSL
class BServiceConfigBuilder internal constructor() : BServiceConfigProps {
    override var debug: Boolean = false

    private val _serviceSuppliers: MutableMap<KClass<*>, ServiceSupplier<*>> = hashMapOf()
    override val serviceSuppliers: Map<KClass<*>, ServiceSupplier<*>> = _serviceSuppliers.unmodifiableView()

    /**
     * Registers a supplier which gets loaded in the same manner as annotated service classes/factories.
     *
     * **Note:** The [annotations] passed will not be readable using standard reflection,
     * they are only read when functions
     * like [ServiceContainer.findAnnotationOnService] or [ServiceContainer.getServiceNamesForAnnotation] are used.
     *
     * @param primaryType     The type as which the service will be *registered* as
     * @param name            The [name][ServiceName] to register the service as
     * @param additionalTypes [Additional types][ServiceType] this service can be *retrieved* as
     * @param isPrimary       Whether this service should be a [primary][Primary] service
     * @param isLazy          Whether this service should be initialized only [when requested][Lazy]
     * @param priority        The [priority][ServicePriority] of this service
     * @param annotations     Annotations which should be tied to this service
     * @param supplier        The function supplying the service
     */
    @JvmSynthetic
    fun <T : Any> registerServiceSupplier(
        primaryType: KClass<T>,
        name: String = ServiceSupplier.defaultName(primaryType),
        additionalTypes: Set<KClass<in T>> = emptySet(), // Accept superclasses not subclasses
        isPrimary: Boolean = false,
        isLazy: Boolean = false,
        priority: Int = 0,
        annotations: List<Annotation> = emptyList(),
        supplier: (BContext) -> T,
    ) {
        registerServiceSupplier(ServiceSupplier(primaryType, name, additionalTypes, isPrimary, isLazy, priority, annotations, supplier))
    }

    /**
     * Registers a supplier which gets loaded in the same manner as annotated service classes/factories.
     *
     * @param supplier The [ServiceSupplier] instance, construct one with [ServiceSupplierBuilder]
     *
     * @see ServiceSupplierBuilder
     */
    fun <T : Any> registerServiceSupplier(supplier: ServiceSupplier<T>) {
        _serviceSuppliers.putIfAbsentOrThrow(supplier.primaryType, supplier) {
            "A supplier is already registered for ${supplier.primaryType.shortQualifiedName}"
        }
    }

    @JvmSynthetic
    internal fun build() = object : BServiceConfig {
        override val debug = this@BServiceConfigBuilder.debug
        override val serviceSuppliers = this@BServiceConfigBuilder.serviceSuppliers.toImmutableMap()
    }
}

/**
 * Registers a supplier which gets loaded in the same manner as annotated service classes/factories.
 *
 * **Note:** The [annotations] passed will not be readable using standard reflection,
 * they are only read when functions
 * like [ServiceContainer.findAnnotationOnService] or [ServiceContainer.getServiceNamesForAnnotation] are used.
 *
 * @param T               The type as which the service will be *registered* as
 * @param name            The [name][ServiceName] to register the service as
 * @param additionalTypes [Additional types][ServiceType] this service can be *retrieved* as
 * @param isPrimary       Whether this service should be a [primary][Primary] service
 * @param isLazy          Whether this service should be initialized only [when requested][Lazy]
 * @param priority        The [priority][ServicePriority] of this service
 * @param annotations     Annotations which should be tied to this service
 * @param supplier        The function supplying the service
 */
inline fun <reified T : Any> BServiceConfigBuilder.registerServiceSupplier(
    name: String = ServiceSupplier.defaultName(T::class),
    additionalTypes: Set<KClass<in T>> = emptySet(), // Accept superclasses not subclasses
    isPrimary: Boolean = false,
    isLazy: Boolean = false,
    priority: Int = 0,
    annotations: List<Annotation> = emptyList(),
    noinline supplier: (BContext) -> T,
) {
    return registerServiceSupplier(T::class, name, additionalTypes, isPrimary, isLazy, priority, annotations, supplier)
}
