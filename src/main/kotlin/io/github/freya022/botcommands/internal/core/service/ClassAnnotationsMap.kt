package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.isSubclassOf
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.utils.annotationRef
import io.github.freya022.botcommands.internal.utils.throwUser
import kotlin.reflect.KClass

/**
 * NOTE: As this only contains annotated classes,
 * this means that service factories are not checked for their annotations.
 * For example, you cannot retrieve services annotated with [@Command][Command],
 * unless the class itself has the annotation
 */
//TODO make a spring variant, see ApplicationContext#getBeanNamesForAnnotation
@BService(priority = Int.MAX_VALUE - 1)
internal class ClassAnnotationsMap(
    serviceBootstrap: ServiceBootstrap,
    instantiableServices: InstantiableServices
) {
    private val instantiableAnnotatedClasses: Map<KClass<out Annotation>, Set<KClass<*>>> = serviceBootstrap.stagingClassAnnotations
        .annotatedClasses
        //Filter out non-instantiable classes
        .mapValues { (_, serviceTypes) ->
            serviceTypes.intersect(instantiableServices.allAvailableTypes)
        }
        .also { serviceBootstrap.clearStagingAnnotationsMap() }

    internal final inline fun <reified A : Annotation> getOrNull(): Set<KClass<*>>? = instantiableAnnotatedClasses[A::class]

    internal final inline fun <reified A : Annotation> get(): Set<KClass<*>> =
        getOrNull<A>() ?: emptySet()

    @Suppress("UNCHECKED_CAST")
    internal final inline fun <reified A : Annotation, reified T : Any> getWithType(): Set<KClass<T>> =
        get<A>().onEach {
            if (!it.isSubclassOf<T>()) {
                throwUser("Class ${it.simpleNestedName} registered as a ${annotationRef<A>()} must extend ${T::class.simpleNestedName}")
            }
        } as Set<KClass<T>>
}