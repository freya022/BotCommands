package com.freya02.botcommands.internal.core.service

import com.freya02.botcommands.api.core.config.BServiceConfig
import com.freya02.botcommands.api.core.service.ServiceError.ErrorType.*
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.core.ClassPathFunction
import com.freya02.botcommands.internal.simpleNestedName
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.toImmutableMap
import mu.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

@BService
internal class InstantiableServiceAnnotationsMap internal constructor(private val context: BContextImpl) {
    //Annotation type match such as: Map<KClass<A>, Map<KClass<*>, A>>
    private val map: Map<KClass<out Annotation>, Map<KClass<*>, Annotation>> = context.serviceAnnotationsMap
        .toImmutableMap()
        //Filter out non-instantiable classes
        .mapValues { (_, map) ->
            map.filterKeys { clazz ->
                val serviceError = context.serviceContainer.canCreateService(clazz) ?: return@filterKeys true

                when (serviceError.errorType) {
                    DYNAMIC_NOT_INSTANTIABLE, INVALID_CONSTRUCTING_FUNCTION, NO_PROVIDER, INVALID_TYPE, UNAVAILABLE_INJECTED_SERVICE, UNAVAILABLE_PARAMETER ->
                        throwUser("Could not load service ${clazz.simpleNestedName}:\n${serviceError.toDetailedString()}")

                    UNAVAILABLE_DEPENDENCY, FAILED_CONDITION -> {
                        if (logger.isTraceEnabled) {
                            logger.trace { "Service ${clazz.simpleNestedName} not loaded:\n${serviceError.toDetailedString()}" }
                        } else if (logger.isDebugEnabled) {
                            logger.debug { "Service ${clazz.simpleNestedName} not loaded: ${serviceError.toSimpleString()}" }
                        }
                    }
                }

                false
            }
        }

    private val functionAnnotationsMap get() = context.getService<FunctionAnnotationsMap>()

    @Suppress("UNCHECKED_CAST")
    internal inline fun <reified A : Annotation> get(): Map<KClass<*>, A>? =
        map[A::class] as Map<KClass<*>, A>?

    internal inline fun <reified A : Annotation> getInstantiableClassesWithAnnotation(): Set<KClass<*>> =
        get<A>()?.keys ?: emptySet()

    internal inline fun <reified CLASS_A : Annotation, reified FUNCTION_A : Annotation> getInstantiableFunctionsWithAnnotation(): List<ClassPathFunction> {
        val classes = getInstantiableClassesWithAnnotation<CLASS_A>()
        val functions = functionAnnotationsMap.getFunctionsWithAnnotation<FUNCTION_A>()

        return functions.filter { it.instance::class in classes }
    }

    @Suppress("UNCHECKED_CAST")
    internal inline fun <reified A : Annotation, reified T : Any> getInstantiableClassesWithAnnotationAndType(): Set<KClass<T>> =
        getInstantiableClassesWithAnnotation<A>().onEach {
            if (!it.isSubclassOf(T::class)) {
                throwUser("Class ${it.simpleNestedName} registered as a @${A::class.simpleNestedName} must extend ${T::class.simpleNestedName}")
            }
        } as Set<KClass<T>>

    internal fun getAllInstantiableClasses() = map.flatMap { (_, annotationReceiversMap) -> annotationReceiversMap.keys }
}

private val logger = KotlinLogging.logger { }

internal class ServiceAnnotationsMap private constructor(
    //Annotation type match such as: Map<KClass<A>, Map<KClass<*>, A>>
    private val map: MutableMap<KClass<out Annotation>, MutableMap<KClass<*>, Annotation>>
) {
    internal constructor() : this(hashMapOf())
    internal constructor(serviceConfig: BServiceConfig) : this(serviceConfig.serviceAnnotationsMap.mapValuesTo(hashMapOf()) { it.value.toMap(hashMapOf()) })

    internal fun <A : Annotation> put(annotationReceiver: KClass<*>, annotationType: KClass<A>, annotation: A) {
        val instanceAnnotationMap = map.computeIfAbsent(annotationType) { hashMapOf() }
        if (annotationReceiver in instanceAnnotationMap) {
            logger.warn("An annotation instance of type '${annotationType.simpleNestedName}' already exists on class '${annotationReceiver.simpleNestedName}'")
            return
        }
        instanceAnnotationMap.putIfAbsent(annotationReceiver, annotation)
    }

    internal fun toImmutableMap() = map.mapValues { it.value.toImmutableMap() }.toImmutableMap()
}
