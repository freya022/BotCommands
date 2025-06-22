package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ServiceType
import io.github.freya022.botcommands.internal.core.service.annotations.RequiresDefaultInjection
import kotlin.reflect.KClass

interface ClassAnnotationsMap {
    fun getOrNull(clazz: KClass<out Annotation>): Set<KClass<*>>?
}

internal inline fun <reified A : Annotation> ClassAnnotationsMap.getOrNull(): Set<KClass<*>>? = getOrNull(A::class)

internal inline fun <reified A : Annotation> ClassAnnotationsMap.get(): Set<KClass<*>> = getOrNull<A>() ?: emptySet()

/**
 * NOTE: As this only contains annotated classes,
 * this means that service factories are not checked for their annotations.
 * For example, you cannot retrieve services annotated with [@Command][Command],
 * unless the class itself has the annotation
 */
@BService(priority = Int.MAX_VALUE - 1)
@ServiceType(ClassAnnotationsMap::class)
@RequiresDefaultInjection
internal class DefaultClassAnnotationsMap(
    instantiableServices: DefaultInstantiableServices
) : ClassAnnotationsMap {
    private val instantiableAnnotatedClasses: Map<KClass<out Annotation>, Set<KClass<*>>> = buildMap {
        instantiableServices
            .availableProviders
            .forEach { provider ->
                val primaryType = provider.primaryType
                provider.annotations.forEach { annotation ->
                    (getOrPut(annotation.annotationClass) { hashSetOf() } as MutableSet<KClass<*>>).add(primaryType)
                }
            }
    }

    override fun getOrNull(clazz: KClass<out Annotation>): Set<KClass<*>>? = instantiableAnnotatedClasses[clazz]
}

