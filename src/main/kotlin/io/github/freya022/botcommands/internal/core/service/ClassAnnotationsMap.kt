package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.lazy
import io.github.freya022.botcommands.api.core.utils.isSubclassOf
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.ClassPathFunction
import io.github.freya022.botcommands.internal.utils.annotationRef
import io.github.freya022.botcommands.internal.utils.throwUser
import kotlin.reflect.KClass

@BService
internal class ClassAnnotationsMap(
    context: BContextImpl,
    instantiableServices: InstantiableServices
) {
    private val functionAnnotationsMap by context.serviceContainer.lazy<FunctionAnnotationsMap>()

    private val instantiableAnnotatedClasses: Map<KClass<out Annotation>, Set<KClass<*>>> = context.serviceAnnotationsMap
        .annotatedClasses
        //Filter out non-instantiable classes
        .mapValues { (_, serviceTypes) ->
            serviceTypes.intersect(instantiableServices.availableServices)
        }

    internal inline fun <reified A : Annotation> get(): Set<KClass<*>>? = instantiableAnnotatedClasses[A::class]

    internal inline fun <reified A : Annotation> getInstantiableClassesWithAnnotation(): Set<KClass<*>> =
        get<A>() ?: emptySet()

    internal inline fun <reified CLASS_A : Annotation, reified FUNCTION_A : Annotation> getInstantiableFunctionsWithAnnotation(): List<ClassPathFunction> {
        val classes = getInstantiableClassesWithAnnotation<CLASS_A>()
        val functions = functionAnnotationsMap.getFunctionsWithAnnotation<FUNCTION_A>()

        return functions.filter { it.instance::class in classes }
    }

    @Suppress("UNCHECKED_CAST")
    internal inline fun <reified A : Annotation, reified T : Any> getInstantiableClassesWithAnnotationAndType(): Set<KClass<T>> =
        getInstantiableClassesWithAnnotation<A>().onEach {
            if (!it.isSubclassOf<T>()) {
                throwUser("Class ${it.simpleNestedName} registered as a ${annotationRef<A>()} must extend ${T::class.simpleNestedName}")
            }
        } as Set<KClass<T>>
}