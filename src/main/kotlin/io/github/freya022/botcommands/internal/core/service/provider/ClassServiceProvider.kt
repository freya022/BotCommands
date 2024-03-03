package io.github.freya022.botcommands.internal.core.service.provider

import io.github.freya022.botcommands.api.core.service.DynamicSupplier
import io.github.freya022.botcommands.api.core.service.DynamicSupplier.Instantiability.InstantiabilityType
import io.github.freya022.botcommands.api.core.service.ServiceError
import io.github.freya022.botcommands.api.core.service.ServiceError.ErrorType
import io.github.freya022.botcommands.api.core.service.ServiceResult
import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.api.core.service.annotations.Lazy
import io.github.freya022.botcommands.api.core.service.annotations.Primary
import io.github.freya022.botcommands.api.core.service.getInterfacedServices
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.core.service.ServiceContainerImpl
import io.github.freya022.botcommands.internal.utils.shortSignature
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.freya022.botcommands.internal.utils.throwService
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KVisibility
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.jvmName

internal class ClassServiceProvider private constructor(
    private val clazz: KClass<*>,
    override var instance: Any?,
    /**
     * If not the sentinel value, the service was attempted to be created.
     */
    private var serviceError: ServiceError?
) : ServiceProvider {
    override val name = clazz.getServiceName()
    override val providerKey = clazz.jvmName
    override val primaryType get() = clazz
    override val types = clazz.getServiceTypes(primaryType)
    override val isPrimary = clazz.hasAnnotation<Primary>()
    override val isLazy = clazz.hasAnnotation<Lazy>()
    override val priority = clazz.getAnnotatedServicePriority()

    private constructor(clazz: KClass<*>) : this(clazz, null, ServiceProvider.nullServiceError)

    private constructor(clazz: KClass<*>, instance: Any) : this(clazz, instance, null)

    override fun canInstantiate(serviceContainer: ServiceContainerImpl): ServiceError? {
        // Returns null if there is no error, the error itself if there's one
        if (serviceError !== ServiceProvider.nullServiceError) return serviceError

        clazz.findAnnotation<InjectedService>()?.let {
            //Skips cache
            return ErrorType.UNAVAILABLE_INJECTED_SERVICE.toError("Tried to load an unavailable InjectedService '${clazz.simpleNestedName}', reason might include: ${it.message}")
        }

        val serviceError = checkInstantiate(serviceContainer)
        //Do not cache service error if a parameter is unavailable, a retrial is allowed
        when (serviceError?.errorType) {
            ErrorType.UNAVAILABLE_PARAMETER, ErrorType.UNAVAILABLE_DEPENDENCY, ErrorType.UNAVAILABLE_INJECTED_SERVICE -> {}

            else -> this.serviceError = serviceError
        }

        return serviceError
    }

    private fun checkInstantiate(serviceContainer: ServiceContainerImpl): ServiceError? {
        require(!clazz.isAbstract) {
            "Cannot provide a service from an abstract class ${clazz.simpleNestedName}"
        }

        clazz.commonCanInstantiate(serviceContainer, clazz)?.let { serviceError -> return serviceError }

        //Is a singleton
        if (clazz.objectInstance != null) return null

        //Check if an instance supplier exists
        if (serviceContainer.context.serviceConfig.instanceSupplierMap[clazz] != null)
            return null

        //Check dynamic suppliers
        serviceContainer.getInterfacedServices<DynamicSupplier>().forEach { dynamicSupplier ->
            val instantiability = dynamicSupplier.getInstantiability(clazz)
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

    override fun createInstance(serviceContainer: ServiceContainerImpl): TimedInstantiation {
        if (instance != null)
            throwInternal("Tried to create an instance of ${clazz.jvmName} when one already exists, instance should be retrieved manually beforehand")

        // Definitely an error if an instance is trying to be created
        // before we know if it's instantiable.
        // We know it's instantiable when the error is null, throw if non-null
        serviceError?.let { serviceError ->
            throwInternal("""
                Tried to create an instance while a service error exists / hasn't been determined
                Provider: $providerKey
                Error: ${serviceError.toSimpleString()}
            """.trimIndent())
        }

        val timedInstantiation = createInstanceNonCached(serviceContainer)
        instance = timedInstantiation.result.getOrNull()
        return timedInstantiation
    }

    private fun createInstanceNonCached(serviceContainer: ServiceContainerImpl): TimedInstantiation {
        measureNullableTimedInstantiation { clazz.objectInstance }?.let { timedInstantiation ->
            return timedInstantiation
        }

        serviceContainer.getInterfacedServices<DynamicSupplier>().forEach { dynamicSupplier ->
            val instantiability = dynamicSupplier.getInstantiability(clazz)
            when (instantiability.type) {
                // This should have been checked in canInstantiate!
                InstantiabilityType.NOT_INSTANTIABLE ->
                    throw IllegalStateException("${dynamicSupplier.javaClass.simpleNestedName} returned '${InstantiabilityType.NOT_INSTANTIABLE.name}' when instantiability test returned '${InstantiabilityType.INSTANTIABLE.name}'! Instantiability should not change over time")

                //Continue looking at other suppliers
                InstantiabilityType.UNSUPPORTED_TYPE -> {}

                //Found a supplier, return instance
                InstantiabilityType.INSTANTIABLE -> return measureTimedInstantiation { dynamicSupplier.get(clazz) }
            }
        }

        //The command object has to be created either by the instance supplier
        // or by the **only** constructor a class has
        // It must resolve all parameter types with the registered parameter suppliers
        val instanceSupplier = serviceContainer.context.serviceConfig.instanceSupplierMap[clazz]
        if (instanceSupplier != null) {
            return measureTimedInstantiation {
                instanceSupplier.supply(serviceContainer.context)
                    ?: throwService("Supplier function in class '${instanceSupplier::class.jvmName}' returned null")
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

    override fun getProviderSignature(): String {
        return clazz.constructors.singleOrNull()?.shortSignature
            ?: clazz.qualifiedName
            ?: clazz.jvmName
    }

    override fun toString() = providerKey

    companion object {
        fun fromClass(clazz: KClass<*>): ClassServiceProvider {
            return ClassServiceProvider(clazz)
        }

        fun fromInstance(type: KClass<*>, instance: Any): ClassServiceProvider {
            return ClassServiceProvider(type, instance)
        }
    }
}

internal fun KClass<*>.getServiceName() = getAnnotatedServiceName() ?: this.simpleNestedName.replaceFirstChar { it.lowercase() }