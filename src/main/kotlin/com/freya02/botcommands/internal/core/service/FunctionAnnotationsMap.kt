package com.freya02.botcommands.internal.core.service

import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.core.ClassPathFunction
import com.freya02.botcommands.internal.simpleNestedName
import com.freya02.botcommands.internal.utils.ReflectionUtils.declaringClass
import com.freya02.botcommands.internal.utils.ReflectionUtils.shortSignature
import mu.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

private val logger = KotlinLogging.logger { }

/**
 * This class holds all functions with at least one annotation
 */
@OptIn(ExperimentalTime::class)
@BService
internal class FunctionAnnotationsMap(context: BContextImpl, instantiableServiceAnnotationsMap: InstantiableServiceAnnotationsMap) {
    private val map: MutableMap<KClass<out Annotation>, MutableMap<KFunction<*>, ClassPathFunction>> = hashMapOf()

    init {
        val duration = measureTime {
            instantiableServiceAnnotationsMap
                .getAllInstantiableClasses()
                .forEach { kClass ->
                    kClass.declaredMemberFunctions.forEach { function ->
                        function.annotations.forEach { annotation ->
                            put(context, function, annotation.annotationClass)
                        }
                    }
                }
        }

        logger.trace { "Functions annotations reflection took ${duration.toDouble(DurationUnit.MILLISECONDS)} ms" }
    }

    private fun <A : Annotation> put(context: BContextImpl, annotationReceiver: KFunction<*>, annotationType: KClass<A>) {
        val instanceAnnotationMap = map.computeIfAbsent(annotationType) { hashMapOf() }
        if (annotationReceiver in instanceAnnotationMap) {
            logger.warn("An annotation instance of type '${annotationType.simpleNestedName}' already exists on function '${annotationReceiver.shortSignature}'")
            return
        }
        instanceAnnotationMap[annotationReceiver] = ClassPathFunction(context, annotationReceiver.declaringClass, annotationReceiver)
    }

    internal fun <A : Annotation> get(annotationClass: KClass<A>): Map<KFunction<*>, ClassPathFunction>? = map[annotationClass]
    internal inline fun <reified A : Annotation> get(): Map<KFunction<*>, ClassPathFunction>? = get(A::class)

    internal fun <A : Annotation> getFunctionsWithAnnotation(annotationClass: KClass<A>): Collection<ClassPathFunction> =
        get(annotationClass)?.values ?: emptySet()
    internal inline fun <reified A : Annotation> getFunctionsWithAnnotation(): Collection<ClassPathFunction> =
        getFunctionsWithAnnotation(A::class)
}