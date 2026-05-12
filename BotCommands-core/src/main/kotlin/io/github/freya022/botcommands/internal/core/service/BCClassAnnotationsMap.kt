package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.RequiresDefaultInjection
import io.github.freya022.botcommands.api.core.service.annotations.ServiceType
import kotlin.reflect.KClass

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
