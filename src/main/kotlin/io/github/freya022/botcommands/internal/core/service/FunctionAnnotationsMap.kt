package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.ClassPathFunction
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.reflect.KClass

@BService(priority = Int.MAX_VALUE - 1)
internal class FunctionAnnotationsMap(
    context: BContextImpl,
    instantiableServices: InstantiableServices,
    private val classAnnotationsMap: ClassAnnotationsMap
) {
    private val map: MutableMap<KClass<out Annotation>, MutableMap<Method, ClassPathFunction>> = hashMapOf()

    init {
        instantiableServices
            // This cannot use "availableServices" as they contain types that would point to the same instance,
            // under a supertype,
            // thus having the same annotated function be returned more than once
            .availablePrimaryTypes
            .forEach { kClass ->
                // The main difference with the kotlin-reflect version
                // is that the Java methods target their declaring class.
                // For example, on ClassA, #wait() in Java is Object#wait()
                // while in kotlin-reflect, it is ClassA#wait()
                // This doesn't matter here since the annotations are going to be the same
                kClass.java.methods.forEach methods@{ method ->
                    if (Modifier.isStatic(method.modifiers)) return@methods

                    method.annotations.forEach { annotation ->
                        put(context, kClass, method, annotation.annotationClass)
                    }
                }
//                kClass.memberFunctions.forEach { function ->
//                    function.annotations.forEach { annotation ->
//                        put(context, kClass, function, annotation.annotationClass)
//                    }
//                }
            }
    }

    private fun <A : Annotation> put(context: BContextImpl, kClass: KClass<*>, method: Method, annotationType: KClass<A>) {
        val instanceAnnotationMap = map.computeIfAbsent(annotationType) { hashMapOf() }
        instanceAnnotationMap.computeIfAbsent(method) { ClassPathFunction(context, kClass, it) }
    }

    internal fun <A : Annotation> get(annotationClass: KClass<A>): Collection<ClassPathFunction> =
        map[annotationClass]?.values ?: emptySet()
    internal inline fun <reified A : Annotation> get(): Collection<ClassPathFunction> =
        get(A::class)

    internal inline fun <reified CLASS_A : Annotation, reified FUNCTION_A : Annotation> getWithClassAnnotation(): List<ClassPathFunction> {
        val classes = classAnnotationsMap.getOrNull<CLASS_A>() ?: return emptyList()
        val functions = get<FUNCTION_A>()

        return functions.filter { it.clazz in classes }
    }
}