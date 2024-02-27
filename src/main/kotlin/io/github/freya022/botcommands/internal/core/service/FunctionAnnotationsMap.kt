package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.ClassPathFunction
import io.github.freya022.botcommands.internal.utils.putIfAbsentOrThrowInternal
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.memberFunctions

private val logger = KotlinLogging.logger { }

@BService(priority = Int.MAX_VALUE - 1)
internal class FunctionAnnotationsMap(context: BContextImpl, instantiableServices: InstantiableServices) {
    private val map: MutableMap<KClass<out Annotation>, MutableMap<KFunction<*>, ClassPathFunction>> = hashMapOf()

    init {
        instantiableServices
            .availableServices
            .forEach { kClass ->
                kClass.memberFunctions.forEach { function ->
                    function.annotations.forEach { annotation ->
                        put(context, kClass, function, annotation.annotationClass)
                    }
                }
            }
    }

    private fun <A : Annotation> put(context: BContextImpl, kClass: KClass<*>, annotationReceiver: KFunction<*>, annotationType: KClass<A>) {
        val instanceAnnotationMap = map.computeIfAbsent(annotationType) { hashMapOf() }
        // An annotation type cannot be present twice on a function, that wouldn't compile
        instanceAnnotationMap.putIfAbsentOrThrowInternal(annotationReceiver, ClassPathFunction(context, kClass, annotationReceiver))
    }

    internal fun <A : Annotation> get(annotationClass: KClass<A>): Map<KFunction<*>, ClassPathFunction>? = map[annotationClass]
    internal inline fun <reified A : Annotation> get(): Map<KFunction<*>, ClassPathFunction>? = get(A::class)

    internal fun <A : Annotation> getFunctionsWithAnnotation(annotationClass: KClass<A>): Collection<ClassPathFunction> =
        get(annotationClass)?.values ?: emptySet()
    internal inline fun <reified A : Annotation> getFunctionsWithAnnotation(): Collection<ClassPathFunction> =
        getFunctionsWithAnnotation(A::class)
}