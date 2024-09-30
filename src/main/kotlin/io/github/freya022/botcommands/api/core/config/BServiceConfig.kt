package io.github.freya022.botcommands.api.core.config

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.InstanceSupplier
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.ServiceSupplier
import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.core.service.annotations.Lazy
import io.github.freya022.botcommands.api.core.service.annotations.Primary
import io.github.freya022.botcommands.api.core.utils.toImmutableMap
import io.github.freya022.botcommands.api.core.utils.unmodifiableView
import io.github.freya022.botcommands.internal.core.config.ConfigDSL
import kotlin.reflect.KClass

@InjectedService
interface BServiceConfig {
    /**
     * Enables debugging of service loading.
     *
     * This includes the operation type (check/create), the run time, the type of the service and where it comes from.
     */
    val debug: Boolean

    @Deprecated("For removal, replaced by serviceSuppliers")
    val instanceSupplierMap: Map<KClass<*>, InstanceSupplier<*>>
    val serviceSuppliers: Map<KClass<*>, ServiceSupplier<*>>
}

@ConfigDSL
class BServiceConfigBuilder internal constructor() : BServiceConfig {
    override var debug: Boolean = false

    private val _instanceSupplierMap: MutableMap<KClass<*>, InstanceSupplier<*>> = hashMapOf()
    @Deprecated("For removal, replaced by serviceSuppliers")
    override val instanceSupplierMap: Map<KClass<*>, InstanceSupplier<*>> = _instanceSupplierMap.unmodifiableView()

    private val _serviceSuppliers: MutableMap<KClass<*>, ServiceSupplier<*>> = hashMapOf()
    override val serviceSuppliers: Map<KClass<*>, ServiceSupplier<*>> = _serviceSuppliers.unmodifiableView()

    /**
     * Registers a supplier lazily returning an instance of the specified class,
     * the instance is then made available via dependency injection.
     *
     * The class it is **registered as** ([T]) is searched for the usual annotations
     * such as [@Primary][Primary], [@InterfacedService][InterfacedService] and [@Lazy][Lazy].
     *
     * **Note:** The class still needs to be in the search path,
     * either using [BConfigBuilder.addSearchPath] or [BConfigBuilder.addClass].
     *
     * @param clazz            The primary type as which the service is registered as, other types may be registered with the usual annotations
     * @param instanceSupplier Supplier for the service instance, ran at startup, unless [clazz] is annotated with [@Lazy][Lazy]
     */
    @Deprecated("For removal, replaced by registerServiceSupplier")
    fun <T : Any> registerInstanceSupplier(clazz: Class<T>, instanceSupplier: InstanceSupplier<T>) {
        _instanceSupplierMap[clazz.kotlin] = instanceSupplier
    }

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
    @JvmOverloads
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
        _serviceSuppliers[primaryType] = ServiceSupplier(primaryType, name, additionalTypes, isPrimary, isLazy, priority, annotations, supplier)
    }

    @JvmSynthetic
    internal fun build() = object : BServiceConfig {
        override val debug = this@BServiceConfigBuilder.debug
        @Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
        override val instanceSupplierMap = this@BServiceConfigBuilder.instanceSupplierMap.toImmutableMap()
        override val serviceSuppliers = this@BServiceConfigBuilder.serviceSuppliers.toImmutableMap()
    }
}

/**
 * Registers a supplier lazily returning an instance of the specified class,
 * the instance is then made available via dependency injection.
 *
 * The class it is **registered as** ([T]) is searched for the usual annotations
 * such as [@Primary][Primary], [@InterfacedService][InterfacedService] and [@Lazy][Lazy].
 *
 * **Note:** The class still needs to be in the search path,
 * either using [BConfigBuilder.addSearchPath] or [BConfigBuilder.addClass].
 *
 * @param T                The primary type as which the service is registered as, other types may be registered with the usual annotations
 * @param instanceSupplier Supplier for the service instance, ran at startup, unless [T] is annotated with [@Lazy][Lazy]
 */
@Suppress("DeprecatedCallableAddReplaceWith", "DEPRECATION")
@Deprecated("For removal, replaced by registerServiceSupplier")
inline fun <reified T : Any> BServiceConfigBuilder.registerInstanceSupplier(instanceSupplier: InstanceSupplier<T>) {
    return registerInstanceSupplier(T::class.java, instanceSupplier)
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
