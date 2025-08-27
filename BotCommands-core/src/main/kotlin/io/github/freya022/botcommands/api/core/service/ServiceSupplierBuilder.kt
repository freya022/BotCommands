package io.github.freya022.botcommands.api.core.service

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.annotations.Primary
import io.github.freya022.botcommands.api.core.service.annotations.ServiceName
import io.github.freya022.botcommands.api.core.service.annotations.ServicePriority
import io.github.freya022.botcommands.api.core.service.annotations.ServiceType
import io.github.freya022.botcommands.api.core.utils.unmodifiableView
import kotlin.reflect.KClass

/**
 * Builder for [ServiceSupplier].
 *
 * @param primaryType The type as which the service will be *registered* as
 */
class ServiceSupplierBuilder<T : Any>(
    private val primaryType: KClass<T>,
) {

    private var name: String? = null
    private var additionalTypes: Set<KClass<in T>> = emptySet() // Accept superclasses not subclasses
    private var isPrimary: Boolean = false
    private var isLazy: Boolean = false
    private var priority: Int = 0
    private var annotations: List<Annotation> = emptyList()

    /**
     * The [name][ServiceName] to register the service as
     */
    fun withName(name: String): ServiceSupplierBuilder<T> = apply {
        this.name = name
    }

    /**
     * [Additional types][ServiceType] this service can be *retrieved* as
     */
    @JvmSynthetic
    fun withAdditionalTypes(vararg additionalTypes: KClass<in T>): ServiceSupplierBuilder<T> = apply {
        this.additionalTypes = additionalTypes.toSet().unmodifiableView()
    }

    /**
     * [Additional types][ServiceType] this service can be *retrieved* as
     */
    @JvmSynthetic
    @JvmName("additionalTypesKt")
    fun withAdditionalTypes(additionalTypes: Collection<KClass<in T>>): ServiceSupplierBuilder<T> = apply {
        this.additionalTypes = additionalTypes.toSet().unmodifiableView()
    }

    /**
     * [Additional types][ServiceType] this service can be *retrieved* as
     */
    fun withAdditionalTypes(vararg additionalTypes: Class<in T>): ServiceSupplierBuilder<T> = apply {
        this.additionalTypes = additionalTypes.mapTo(hashSetOf()) { it.kotlin }.unmodifiableView()
    }

    /**
     * [Additional types][ServiceType] this service can be *retrieved* as
     */
    fun withAdditionalTypes(additionalTypes: Collection<Class<in T>>): ServiceSupplierBuilder<T> = apply {
        this.additionalTypes = additionalTypes.mapTo(hashSetOf()) { it.kotlin }.unmodifiableView()
    }

    /**
     * Whether this service should be a [primary][Primary] service
     */
    @JvmOverloads
    fun asPrimary(primary: Boolean = true): ServiceSupplierBuilder<T> = apply {
        this.isPrimary = primary
    }

    /**
     * Whether this service should be initialized only [when requested][Lazy]
     */
    @JvmOverloads
    fun asLazy(lazy: Boolean = true): ServiceSupplierBuilder<T> = apply {
        this.isLazy = lazy
    }

    /**
     * The [priority][ServicePriority] of this service
     */
    fun withPriority(priority: Int): ServiceSupplierBuilder<T> = apply {
        this.priority = priority
    }

    /**
     * Annotations which should be tied to this service
     */
    fun withAnnotations(vararg annotations: Annotation): ServiceSupplierBuilder<T> = apply {
        this.annotations = annotations.toList().unmodifiableView()
    }

    /**
     * Annotations which should be tied to this service
     */
    fun withAnnotations(annotations: Collection<Annotation>): ServiceSupplierBuilder<T> = apply {
        this.annotations = annotations.toList().unmodifiableView()
    }

    /**
     * Builds the [ServiceSupplier].
     *
     * @param supplier The function supplying the service
     */
    fun build(supplier: (BContext) -> T): ServiceSupplier<T> {
        return ServiceSupplier(
            primaryType,
            name ?: ServiceSupplier.defaultName(primaryType),
            additionalTypes,
            isPrimary,
            isLazy,
            priority,
            annotations,
            supplier,
        )
    }
}
