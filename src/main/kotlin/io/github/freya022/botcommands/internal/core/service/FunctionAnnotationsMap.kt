package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.ClassPathFunction
import io.github.freya022.botcommands.internal.utils.putIfAbsentOrThrowInternal
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.memberFunctions

@BService(priority = Int.MAX_VALUE - 1)
internal class FunctionAnnotationsMap(
    context: BContextImpl,
    instantiableServices: InstantiableServices,
    private val classAnnotationsMap: ClassAnnotationsMap
) {
    private val map: MutableMap<KClass<out Annotation>, MutableMap<KFunction<*>, ClassPathFunction>> = hashMapOf()

    init {
        instantiableServices
            // This cannot use "availableServices" as they contain types that would point to the same instance,
            // under a supertype,
            // thus having the same annotated function be returned more than once
            .availablePrimaryTypes
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

    internal fun <A : Annotation> get(annotationClass: KClass<A>): Collection<ClassPathFunction> =
        map[annotationClass]?.values ?: emptySet()
    internal final inline fun <reified A : Annotation> get(): Collection<ClassPathFunction> =
        get(A::class)

    internal final inline fun <reified CLASS_A : Annotation, reified FUNCTION_A : Annotation> getWithClassAnnotation(): List<ClassPathFunction> {
        val classes = classAnnotationsMap.getOrNull<CLASS_A>() ?: return emptyList()
        val functions = get<FUNCTION_A>()

        return functions.filter { it.clazz in classes }
    }
}