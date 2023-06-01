package com.freya02.botcommands.internal.core.service

import com.freya02.botcommands.api.core.config.BServiceConfig
import com.freya02.botcommands.internal.simpleNestedName
import com.freya02.botcommands.internal.toImmutableMap
import mu.KotlinLogging
import kotlin.reflect.KClass

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
    internal fun <T : Annotation> get(annotationClass: KClass<out T>): Map<KClass<*>, T>? =
        map[annotationClass] as Map<KClass<*>, T>?

    internal fun <T : Annotation> getClassesWithAnnotation(annotationClass: KClass<out T>): Set<KClass<*>> =
        get(annotationClass)?.keys ?: emptySet()

    internal fun getAllClasses() = map.flatMap { (_, annotationReceiversMap) -> annotationReceiversMap.keys }

    internal fun toImmutableMap() = map.mapValues { it.value.toImmutableMap() }.toImmutableMap()
}
