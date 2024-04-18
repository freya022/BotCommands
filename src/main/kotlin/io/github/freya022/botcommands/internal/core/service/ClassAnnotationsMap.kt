package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ServiceType
import io.github.freya022.botcommands.internal.core.service.annotations.RequiresDefaultInjection
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

internal sealed interface ClassAnnotationsMap {
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
    bootstrap: DefaultBotCommandsBootstrap,
    instantiableServices: InstantiableServices
) : ClassAnnotationsMap {
    private val instantiableAnnotatedClasses: Map<KClass<out Annotation>, Set<KClass<*>>> = bootstrap.stagingClassAnnotations
        .annotatedClasses
        //Filter out non-instantiable classes
        .mapValues { (_, serviceTypes) ->
            serviceTypes.intersect(instantiableServices.allAvailableTypes)
        }
        .also { bootstrap.clearStagingAnnotationsMap() }

    override fun getOrNull(clazz: KClass<out Annotation>): Set<KClass<*>>? = instantiableAnnotatedClasses[clazz]
}

@Service
@DependsOn("springBotCommandsBootstrap") // Forces reflection metadata to be scanned first
internal class SpringClassAnnotationsMap(
    private val context: ApplicationContext
) : ClassAnnotationsMap {
    override fun getOrNull(clazz: KClass<out Annotation>): Set<KClass<*>>? {
        val beansWithAnnotation = context.getBeansWithAnnotation(clazz.java)
        if (beansWithAnnotation.isEmpty()) return null

        return beansWithAnnotation.keys.mapTo(hashSetOf()) { context.getType(it).kotlin }
    }
}