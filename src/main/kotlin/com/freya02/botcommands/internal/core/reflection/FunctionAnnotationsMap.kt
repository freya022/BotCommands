package com.freya02.botcommands.internal.core.reflection

import com.freya02.botcommands.internal.simpleNestedName
import com.freya02.botcommands.internal.utils.ReflectionUtils.shortSignature
import mu.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

private val logger = KotlinLogging.logger { }

/**
 * This class holds all functions with at least one annotation
 */
internal class FunctionAnnotationsMap {
    private val map: MutableMap<KClass<out Annotation>, MutableMap<KFunction<*>, Annotation>> = hashMapOf()

    internal fun <A : Annotation> put(annotationReceiver: KFunction<*>, annotationType: KClass<A>, annotation: A) {
        val instanceAnnotationMap = map.computeIfAbsent(annotationType) { hashMapOf() }
        if (annotationReceiver in instanceAnnotationMap) {
            logger.warn("An annotation instance of type '${annotationType.simpleNestedName}' already exists on function '${annotationReceiver.shortSignature}'")
            return
        }
        instanceAnnotationMap.putIfAbsent(annotationReceiver, annotation)
    }

    @Suppress("UNCHECKED_CAST")
    internal inline fun <reified A : Annotation> get(): Map<KFunction<*>, A>? =
        map[A::class] as Map<KFunction<*>, A>?

    internal inline fun <reified A : Annotation> getFunctionsWithAnnotation(): Set<KFunction<*>> =
        get<A>()?.keys ?: emptySet()
}