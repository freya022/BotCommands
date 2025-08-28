package io.github.freya022.botcommands.api.core.service

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.config.BServiceConfigBuilder
import io.github.freya022.botcommands.api.core.service.annotations.*
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import kotlin.reflect.KClass

/**
 * Represents the attributes and initializer of a given service type.
 *
 * **Note:** The [annotations] passed will not be readable using standard reflection,
 * they are only read when functions
 * like [ServiceContainer.findAnnotationOnService] or [ServiceContainer.getServiceNamesForAnnotation] are used.
 *
 * @property primaryType     The type as which the service will be *registered* as
 * @property name            The [name][ServiceName] to register the service as
 * @property additionalTypes [Additional types][ServiceType] this service can be *retrieved* as
 * @property isPrimary       Whether this service should be a [primary][Primary] service
 * @property isLazy          Whether this service should be initialized only [when requested][Lazy]
 * @property priority        The [priority][ServicePriority] of this service
 * @property annotations     Annotations which should be tied to this service
 * @property supplier        The function supplying the service
 *
 * @see BServiceConfigBuilder.registerServiceSupplier
 */
class ServiceSupplier<T : Any>(
    val primaryType: KClass<T>,
    val name: String = defaultName(primaryType),
    val additionalTypes: Set<KClass<in T>> = emptySet(), // Accept superclasses not subclasses
    val isPrimary: Boolean = false,
    val isLazy: Boolean = false,
    val priority: Int = 0,
    val annotations: List<Annotation> = emptyList(),
    val supplier: (BContext) -> T,
) {

    override fun toString(): String {
        return "ServiceSupplier(primaryType=$primaryType, name='$name', additionalTypes=$additionalTypes, isPrimary=$isPrimary, isLazy=$isLazy, priority=$priority, annotations=$annotations)"
    }

    companion object {
        @JvmStatic
        fun defaultName(clazz: KClass<*>): String {
            return clazz.simpleNestedName.replaceFirstChar { it.lowercase() }
        }

        @JvmStatic
        fun defaultName(clazz: Class<*>): String {
            return clazz.simpleNestedName.replaceFirstChar { it.lowercase() }
        }

        /**
         * Creates a [ServiceSupplierBuilder].
         *
         * @param primaryType The type as which the service will be *registered* as
         */
        @JvmStatic
        fun <T : Any> builder(primaryType: KClass<T>): ServiceSupplierBuilder<T> {
            return ServiceSupplierBuilder(primaryType)
        }

        /**
         * Creates a [ServiceSupplierBuilder].
         *
         * @param primaryType The type as which the service will be *registered* as
         */
        @JvmStatic
        fun <T : Any> builder(primaryType: Class<T>): ServiceSupplierBuilder<T> {
            return ServiceSupplierBuilder(primaryType.kotlin)
        }
    }
}
