package io.github.freya022.botcommands.internal.core.service

import io.github.classgraph.ClassInfo
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.config.BServiceConfig
import io.github.freya022.botcommands.api.core.service.ClassGraphProcessor
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
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmName

@BService(priority = Int.MAX_VALUE)
internal class InstantiableServiceAnnotationsMap internal constructor(private val context: BContextImpl) {
    private class InterfacedType(val clazz: KClass<*>, annotation: InterfacedService) {
        val acceptMultiple = annotation.acceptMultiple

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as InterfacedType

            return clazz == other.clazz
        }

        override fun hashCode(): Int {
            return clazz.hashCode()
        }
    }

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
        val typeToImplementations = hashMapOf<InterfacedType, MutableSet<ServiceProvider>>()
        getAllInstantiableClasses().forEach { kClass ->
            // For each service, take their implemented interfaced services
            // and put them in a map as to figure out if multiple - instantiable - implementation exists
            val provider = context.serviceProviders.findForType(kClass)
                ?: throwInternal("Could not find back service provider for ${kClass.simpleNestedName}")
            val interfacedTypes = provider.types.mapNotNull { clazz ->
                clazz.findAnnotation<InterfacedService>()?.let { InterfacedType(clazz, it) }
            }
            //Only check those implementing an interfaced service
            if (interfacedTypes.isEmpty()) return@forEach
            if (provider.canInstantiate(context.serviceContainer) != null) return@forEach

            interfacedTypes.forEach { interfacedType ->
                typeToImplementations
                    .computeIfAbsent(interfacedType) { hashSetOf() }
                    .add(provider)
            }
        }

        val nonUniqueImplementations = typeToImplementations
            // Only keep single-implementation interfaced service which have multiple implementations
            .filter { (interfacedType, providers) -> !interfacedType.acceptMultiple && providers.size > 1 }
        if (nonUniqueImplementations.isNotEmpty()) {
            val message = buildString {
                appendLine("Interfaced services with 'acceptMultiple = false' cannot have multiple implementations, " +
                        "please adjust your services so at most one implementation is instantiable:")

                nonUniqueImplementations.forEach { (interfacedServiceType, implementations) ->
                    appendLine("${interfacedServiceType.clazz.simpleNestedName}: ${implementations.joinToString { it.providerKey }}")
                }
            }
            throw IllegalStateException(message)
        }

        typeToImplementations.forEach { (interfacedType, providers) ->
            logger.debug { "Found implementations of ${interfacedType.clazz.simpleNestedName} in ${providers.joinToString { it.primaryType.simpleNestedName }}" }
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

internal class ServiceAnnotationsMapProcessor internal constructor(
    private val config: BConfig,
    private val serviceAnnotationsMap: ServiceAnnotationsMap
) : ClassGraphProcessor {
    override fun processClass(context: BContext, classInfo: ClassInfo, kClass: KClass<*>, isService: Boolean) {
        //Fill map with all the @Command, @Resolver, etc... declarations
        if (isService) {
            classInfo.annotationInfo.forEach { annotationInfo ->
                if (config.serviceConfig.serviceAnnotations.any { it.jvmName == annotationInfo.name }) {
                    serviceAnnotationsMap.put(
                        annotationReceiver = kClass,
                        annotationType = annotationInfo.classInfo.loadClass(Annotation::class.java).kotlin,
                        annotation = annotationInfo.loadClassAndInstantiate()
                    )
                }
            }
        }
    }
}