package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.core.config.BServiceConfig
import io.github.freya022.botcommands.api.core.service.ServiceError.ErrorType.*
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.core.utils.toImmutableMap
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.ClassPathFunction
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.freya022.botcommands.internal.utils.throwUser
import mu.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf

@BService(priority = Int.MAX_VALUE)
internal class InstantiableServiceAnnotationsMap internal constructor(private val context: BContextImpl) {
    //Annotation type match such as: Map<KClass<A>, Map<KClass<*>, A>>
    private val map: Map<KClass<out Annotation>, Map<KClass<*>, Annotation>> = context.serviceAnnotationsMap
        .toImmutableMap()
        //Filter out non-instantiable classes
        .mapValues { (_, map) ->
            map.filterKeys { clazz ->
                val serviceError = context.serviceContainer.canCreateService(clazz) ?: return@filterKeys true

                when (serviceError.errorType) {
                    DYNAMIC_NOT_INSTANTIABLE, INVALID_CONSTRUCTING_FUNCTION, NO_PROVIDER, INVALID_TYPE, UNAVAILABLE_INJECTED_SERVICE, UNAVAILABLE_PARAMETER, FAILED_FATAL_CUSTOM_CONDITION ->
                        throwUser("Could not load service ${clazz.simpleNestedName}:\n${serviceError.toDetailedString()}")

                    UNAVAILABLE_DEPENDENCY, FAILED_CONDITION, FAILED_CUSTOM_CONDITION -> {
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

    init {
        val singularTypeToImplementationsMap = hashMapOf<KClass<*>, MutableSet<ServiceProvider>>()
        getAllInstantiableClasses().forEach { kClass ->
            // For each service, take their implemented interfaced services
            // and put them in a map as to figure out if multiple - instantiable - implementation exists
            val provider = context.serviceProviders.findForType(kClass)
                ?: throwInternal("Could not find back service provider for ${kClass.simpleNestedName}")
            if (provider.canInstantiate(context.serviceContainer) != null) return@forEach

            provider.types
                // Keep single-implementation interfaced services.
                // Do not take non-interfaced services as single-implementations,
                // as they can use names to differentiate each others
                .filter { it.findAnnotation<InterfacedService>()?.acceptMultiple == false }
                .forEach { interfacedServiceType ->
                    singularTypeToImplementationsMap
                        .computeIfAbsent(interfacedServiceType) { hashSetOf() }
                        .add(provider)
                }
        }

        val nonUniqueImplementations = singularTypeToImplementationsMap.filterValues { it.size > 1 }
        if (nonUniqueImplementations.isNotEmpty()) {
            val message = buildString {
                appendLine("Interfaced services with 'acceptMultiple = false' cannot have multiple implementations, " +
                        "please adjust your services so at most one implementation is instantiable:")

                nonUniqueImplementations.forEach { (interfacedServiceType, implementations) ->
                    appendLine("${interfacedServiceType.simpleNestedName}: ${implementations.joinToString { it.providerKey }}")
                }
            }
            throw IllegalStateException(message)
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
