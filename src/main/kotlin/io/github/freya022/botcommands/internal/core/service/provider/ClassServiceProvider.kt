package io.github.freya022.botcommands.internal.core.service.provider

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.*
import io.github.freya022.botcommands.api.core.service.DynamicSupplier.Instantiability.InstantiabilityType
import io.github.freya022.botcommands.api.core.service.ServiceError.ErrorType
import io.github.freya022.botcommands.api.core.service.annotations.Lazy
import io.github.freya022.botcommands.api.core.service.annotations.Primary
import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.core.exceptions.ServiceException
import io.github.freya022.botcommands.internal.core.service.DefaultServiceContainerImpl
import io.github.freya022.botcommands.internal.utils.*
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.resolveBestReference
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KVisibility
import kotlin.reflect.jvm.jvmName

private val logger = KotlinLogging.logger { }

internal class ClassServiceProvider internal constructor(
    private val clazz: KClass<*>
) : ServiceProvider {
    init {
        require(!clazz.isAbstract) {
            "Abstract class '${clazz.simpleNestedName}' cannot be constructed"
        }
    }

    override var instance: Any? = null
    /**
     * If not the sentinel value, the service was attempted to be created.
     */
    private var serviceError: ServiceError? = ServiceProvider.nullServiceError

    override val annotations = clazz.getAllAnnotations()
    override val name = getServiceName(clazz)
    override val providerKey = clazz.jvmName
    override val primaryType get() = clazz
    override val types = getServiceTypes(primaryType)
    override val isPrimary = hasAnnotation<Primary>()
    override val isLazy = hasAnnotation<Lazy>()
    override val priority = getAnnotatedServicePriority()

    override fun canInstantiate(serviceContainer: DefaultServiceContainerImpl): ServiceError? {
        // Returns null if there is no error, the error itself if there's one
        if (serviceError !== ServiceProvider.nullServiceError) return serviceError

        val serviceError = checkInstantiate(serviceContainer)
        //Do not cache service error if a parameter is unavailable, a retrial is allowed
        when (serviceError?.errorType) {
            ErrorType.UNAVAILABLE_PARAMETER, ErrorType.UNAVAILABLE_DEPENDENCY -> {}

            else -> this.serviceError = serviceError
        }

        return serviceError
    }

    private fun checkInstantiate(serviceContainer: DefaultServiceContainerImpl): ServiceError? {
        commonCanInstantiate(serviceContainer, clazz)?.let { serviceError -> return serviceError }

        //Is a singleton
        if (clazz.isObject) return null

        //Check if an instance supplier exists
        if (serviceContainer.serviceConfig.instanceSupplierMap[clazz] != null)
            return null

        //Check dynamic suppliers
        serviceContainer.getInterfacedServices<DynamicSupplier>().forEach { dynamicSupplier ->
            val instantiability = dynamicSupplier.getInstantiability(clazz, name)
            when (instantiability.type) {
                //Return error message
                InstantiabilityType.NOT_INSTANTIABLE ->
                    return ErrorType.DYNAMIC_NOT_INSTANTIABLE.toError(
                        errorMessage = instantiability.message!!,
                        extraMessage = "${dynamicSupplier::class.simpleNestedName} failed"
                    )
                //Continue looking at other suppliers
                InstantiabilityType.UNSUPPORTED_TYPE -> {}
                //Found a supplier, return no error message
                InstantiabilityType.INSTANTIABLE -> return null
            }
        }

        //Check constructor parameters
        //It's fine if there's no constructor, it just means it's not instantiable
        val constructingFunction = findConstructingFunction(clazz).let { it.getOrNull() ?: return it.serviceError }
        constructingFunction.checkConstructingFunction(serviceContainer)?.let { serviceError -> return serviceError }

        return null
    }

    override fun createInstance(serviceContainer: DefaultServiceContainerImpl): TimedInstantiation<*> {
        if (instance != null)
            throwInternal("Tried to create an instance of ${clazz.jvmName} when one already exists, instance should be retrieved manually beforehand")

        // Definitely an error if an instance is trying to be created
        // before we know if it's instantiable.
        // We know it's instantiable when the error is null, throw if non-null
        serviceError?.let { serviceError ->
            throwInternal("""
                Tried to create an instance while a service error exists / hasn't been determined
                Provider: ${getProviderSignature()}
                Error: ${serviceError.toSimpleString()}
            """.trimIndent())
        }

        val timedInstantiation = createInstanceNonCached(serviceContainer)
        instance = timedInstantiation.instance
        return timedInstantiation
    }

    private fun createInstanceNonCached(serviceContainer: DefaultServiceContainerImpl): TimedInstantiation<*> {
        measureNullableTimedInstantiation { clazz.objectInstance }?.let { timedInstantiation ->
            return timedInstantiation
        }

        serviceContainer.getInterfacedServices<DynamicSupplier>().forEach { dynamicSupplier ->
            val instantiability = dynamicSupplier.getInstantiability(clazz, name)
            when (instantiability.type) {
                // This should have been checked in canInstantiate!
                InstantiabilityType.NOT_INSTANTIABLE ->
                    throwState("${dynamicSupplier.javaClass.simpleNestedName} returned '${InstantiabilityType.NOT_INSTANTIABLE.name}' when instantiability test returned '${InstantiabilityType.INSTANTIABLE.name}'! Instantiability should not change over time")

                //Continue looking at other suppliers
                InstantiabilityType.UNSUPPORTED_TYPE -> {}

                //Found a supplier, return instance
                InstantiabilityType.INSTANTIABLE -> return measureTimedInstantiation { dynamicSupplier.get(clazz, name) }
            }
        }

        //The command object has to be created either by the instance supplier
        // or by the **only** constructor a class has
        // It must resolve all parameter types with the registered parameter suppliers
        val instanceSupplier = serviceContainer.serviceConfig.instanceSupplierMap[clazz]
        if (instanceSupplier != null) {
            return measureTimedInstantiation {
                instanceSupplier.supply(serviceContainer.getService<BContext>())
                    ?: throw ServiceException(
                        ErrorType.PROVIDER_RETURNED_NULL.toError(
                            errorMessage = "Supplier function in class '${instanceSupplier.javaClass.simpleNestedName}' returned null",
                            failedFunction = instanceSupplier::supply.resolveBestReference()
                        )
                    )
            }
        }

        val constructingFunction = findConstructingFunction(clazz).getOrThrow()

        return constructingFunction.callConstructingFunction(serviceContainer)
    }

    private fun findConstructingFunction(clazz: KClass<*>): ServiceResult<KFunction<*>> {
        val constructors = clazz.constructors
        if (constructors.isEmpty())
            return ErrorType.INVALID_CONSTRUCTING_FUNCTION.toResult("Class ${clazz.simpleNestedName} must have an accessible constructor")
        if (constructors.size != 1)
            return ErrorType.INVALID_CONSTRUCTING_FUNCTION.toResult("Class ${clazz.simpleNestedName} must have exactly one constructor")

        val constructor = constructors.single()
        if (constructor.visibility != KVisibility.PUBLIC && constructor.visibility != KVisibility.INTERNAL) {
            return ErrorType.INVALID_CONSTRUCTING_FUNCTION.toResult("Constructor of ${clazz.simpleNestedName} must be public")
        }

        return ServiceResult.pass(constructor)
    }

    override fun getProviderFunction(): KFunction<*>? {
        if (clazz.isObject) return null

        val constructor = clazz.constructors.firstOrNull()
        if (constructor == null) logger.warn { "No constructor in ${clazz.shortQualifiedName}" }

        return constructor
    }

    override fun getProviderSignature(): String {
        if (clazz.isObject) return "<object ${clazz.shortQualifiedName}>"
        return getProviderFunction()?.shortSignature ?: "<no-provider ${clazz.shortQualifiedName}>"
    }

    override fun toString() = providerKey
}

@PublishedApi
internal fun ServiceProvider.getServiceName(clazz: KClass<*>) =
    getAnnotatedServiceName() ?: clazz.simpleNestedName.replaceFirstChar { it.lowercase() }