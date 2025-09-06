package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.RequiresDefaultInjection
import io.github.freya022.botcommands.api.core.service.annotations.ServiceType
import kotlin.reflect.KClass

/**
 * NOTE: As this only contains annotated classes,
 * this means that service factories are not checked for their annotations.
 * For example, you cannot retrieve services annotated with [@Command][io.github.freya022.botcommands.api.commands.annotations.Command],
 * unless the class itself has the annotation
 */
@BService(priority = Int.MAX_VALUE - 1)
@ServiceType(ClassAnnotationsMap::class)
@RequiresDefaultInjection
internal class BCClassAnnotationsMap(
    instantiableServices: BCInstantiableServices
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
