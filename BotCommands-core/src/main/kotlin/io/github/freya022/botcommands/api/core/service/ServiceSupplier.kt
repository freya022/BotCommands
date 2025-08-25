package io.github.freya022.botcommands.api.core.service

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.config.BServiceConfigBuilder
import io.github.freya022.botcommands.api.core.service.annotations.*
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.core.annotations.SkipJavaReflectionOverload
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
class ServiceSupplier<T : Any> @JvmOverloads constructor(
    val primaryType: KClass<T>,
    val name: String = defaultName(primaryType),
    val additionalTypes: Set<KClass<in T>> = emptySet(), // Accept superclasses not subclasses
    val isPrimary: Boolean = false,
    val isLazy: Boolean = false,
    val priority: Int = 0,
    val annotations: List<Annotation> = emptyList(),
    val supplier: (BContext) -> T,
) {

    /**
     * Constructs a [ServiceSupplier].
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
    constructor(
        primaryType: Class<T>,
        name: String = defaultName(primaryType.kotlin),
        additionalTypes: Set<Class<in T>> = emptySet(), // Accept superclasses not subclasses
        isPrimary: Boolean = false,
        isLazy: Boolean = false,
        priority: Int = 0,
        annotations: List<Annotation> = emptyList(),
        supplier: (BContext) -> T,
    ) : this(primaryType.kotlin, name, additionalTypes.mapTo(hashSetOf()) { it.kotlin }, isPrimary, isLazy, priority, annotations, supplier)

    override fun toString(): String {
        return "ServiceSupplier(primaryType=$primaryType, name='$name', additionalTypes=$additionalTypes, isPrimary=$isPrimary, isLazy=$isLazy, priority=$priority, annotations=$annotations)"
    }

    companion object {
        @SkipJavaReflectionOverload
        fun defaultName(clazz: KClass<*>): String {
            return clazz.simpleNestedName.replaceFirstChar { it.lowercase() }
        }

        @JvmStatic
        fun defaultName(clazz: Class<*>): String {
            return clazz.simpleNestedName.replaceFirstChar { it.lowercase() }
        }
    }
}
