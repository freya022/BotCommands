package io.github.freya022.botcommands.api.core.service

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import kotlin.reflect.KClass

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
        fun defaultName(clazz: KClass<*>): String {
            return clazz.simpleNestedName.replaceFirstChar { it.lowercase() }
        }

        @JvmStatic
        fun defaultName(clazz: Class<*>): String {
            return clazz.simpleNestedName.replaceFirstChar { it.lowercase() }
        }
    }
}