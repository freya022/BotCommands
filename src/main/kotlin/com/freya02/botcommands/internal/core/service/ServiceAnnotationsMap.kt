package com.freya02.botcommands.internal.core.service

import com.freya02.botcommands.api.core.config.BServiceConfig
import com.freya02.botcommands.internal.simpleNestedName
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.toImmutableMap
import mu.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

private val logger = KotlinLogging.logger { }

internal class ServiceAnnotationsMap private constructor(
    //Annotation type match such as: Map<KClass<A>, Map<KClass<*>, A>>
    private val map: MutableMap<KClass<out Annotation>, MutableMap<KClass<*>, Annotation>>
) {
    internal constructor() : this(hashMapOf())
    internal constructor(serviceConfig: BServiceConfig) : this(serviceConfig.serviceAnnotationsMap.mapValuesTo(hashMapOf()) { it.value.toMap(hashMapOf()) })

    internal fun <A : Annotation> put(annotationReceiver: KClass<*>, annotationType: KClass<A>, annotation: A) {
        val instanceAnnotationMap = map.computeIfAbsent(annotationType) { hashMapOf() }
        if (annotationReceiver in instanceAnnotationMap) {
            logger.warn("An annotation instance of type '${annotationType.simpleNestedName}' already exists on class '${annotationReceiver.simpleNestedName}'")
            return
        }
        instanceAnnotationMap.putIfAbsent(annotationReceiver, annotation)
    }

    @Suppress("UNCHECKED_CAST")
    internal inline fun <reified A : Annotation> get(): Map<KClass<*>, A>? =
        map[A::class] as Map<KClass<*>, A>?

    internal inline fun <reified A : Annotation> getClassesWithAnnotation(): Set<KClass<*>> =
        get<A>()?.keys ?: emptySet()

    @Suppress("UNCHECKED_CAST")
    internal inline fun <reified A : Annotation, reified T : Any> getClassesWithAnnotationAndType(): Set<KClass<T>> =
        getClassesWithAnnotation<A>().onEach {
            if (!it.isSubclassOf(T::class)) {
                throwUser("Class ${it.simpleNestedName} registered as a @${A::class.simpleNestedName} must extend ${T::class.simpleNestedName}")
            }
        } as Set<KClass<T>>

    internal fun getAllClasses() = map.flatMap { (_, annotationReceiversMap) -> annotationReceiversMap.keys }

    internal fun toImmutableMap() = map.mapValues { it.value.toImmutableMap() }.toImmutableMap()
}
