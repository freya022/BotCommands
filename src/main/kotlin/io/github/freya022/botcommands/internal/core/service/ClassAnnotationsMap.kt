package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.isSubclassOf
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.utils.annotationRef
import io.github.freya022.botcommands.internal.utils.throwUser
import kotlin.reflect.KClass

@BService(priority = Int.MAX_VALUE - 1)
internal class ClassAnnotationsMap(
    context: BContextImpl,
    instantiableServices: InstantiableServices
) {
    private val instantiableAnnotatedClasses: Map<KClass<out Annotation>, Set<KClass<*>>> = context.serviceAnnotationsMap
        .annotatedClasses
        //Filter out non-instantiable classes
        .mapValues { (_, serviceTypes) ->
            serviceTypes.intersect(instantiableServices.availableServices)
        }
        .also { context.clearServiceAnnotationsMap() }

    internal inline fun <reified A : Annotation> getOrNull(): Set<KClass<*>>? = instantiableAnnotatedClasses[A::class]

    internal inline fun <reified A : Annotation> get(): Set<KClass<*>> =
        getOrNull<A>() ?: emptySet()

    @Suppress("UNCHECKED_CAST")
    internal inline fun <reified A : Annotation, reified T : Any> getInstantiableClassesWithAnnotationAndType(): Set<KClass<T>> =
        get<A>().onEach {
            if (!it.isSubclassOf<T>()) {
                throwUser("Class ${it.simpleNestedName} registered as a ${annotationRef<A>()} must extend ${T::class.simpleNestedName}")
            }
        } as Set<KClass<T>>
}