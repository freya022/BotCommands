package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.core.service.DynamicSupplier
import io.github.freya022.botcommands.api.core.service.DynamicSupplier.Instantiability.InstantiabilityType
import io.github.freya022.botcommands.api.core.service.ServiceError
import io.github.freya022.botcommands.api.core.service.ServiceError.ErrorType
import io.github.freya022.botcommands.api.core.service.ServiceResult
import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.api.core.service.getInterfacedServices
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.freya022.botcommands.internal.utils.throwService
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KVisibility
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmName

internal class ClassServiceProvider(
    private val clazz: KClass<*>,
    override var instance: Any? = null
) : ServiceProvider {
    override val name = clazz.getServiceName()
    override val providerKey = clazz.jvmName
    override val primaryType get() = clazz
    override val types = clazz.getServiceTypes(primaryType)
    override val priority = clazz.getAnnotatedServicePriority()

    /**
     * If not the sentinel value, the service was attempted to be created.
     */
    private var serviceError: ServiceError? = ServiceProvider.nullServiceError

    override fun canInstantiate(serviceContainer: ServiceContainerImpl): ServiceError? {
        // Returns null if there is no error, the error itself if there's one
        if (serviceError !== ServiceProvider.nullServiceError) return serviceError

        clazz.findAnnotation<InjectedService>()?.let {
            //Skips cache
            return ErrorType.UNAVAILABLE_INJECTED_SERVICE.toError("Tried to load an unavailable InjectedService '${clazz.simpleNestedName}', reason might include: ${it.message}")
        }

        serviceError = checkInstantiate(serviceContainer)
        return serviceError
    }

    private fun checkInstantiate(serviceContainer: ServiceContainerImpl): ServiceError? {
        clazz.commonCanInstantiate(serviceContainer, clazz)?.let { serviceError -> return serviceError }

        //Is a singleton
        if (clazz.objectInstance != null) return null

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
        // Definitely an error if an instance is trying to be created
        // before we know if it's instantiable.
        // We know it's instantiable when the error is null, throw if non-null
        serviceError?.let { serviceError ->
            throwInternal("""
                Tried to create an instance while a service error exists / hasn't been determined
                Provider: $providerKey
                Instance: $instance
                Error: ${serviceError.toSimpleString()}
            """.trimIndent())
        }

        measureNullableTimedInstantiation { clazz.objectInstance }?.let { timedInstantiation ->
            return timedInstantiation
        }

        serviceContainer.getInterfacedServices<DynamicSupplier>().forEach { dynamicSupplier ->
            val instantiability = dynamicSupplier.getInstantiability(clazz)
            when (instantiability.type) {
                //Return error message
                InstantiabilityType.NOT_INSTANTIABLE -> ErrorType.DYNAMIC_NOT_INSTANTIABLE.toResult<Any>(instantiability.message!!, "${dynamicSupplier::class.simpleNestedName} failed")
                    .toFailedTimedInstantiation()
                //Continue looking at other suppliers
                InstantiabilityType.UNSUPPORTED_TYPE -> {}
                //Found a supplier, return instance
                InstantiabilityType.INSTANTIABLE -> return measureTimedInstantiation {
                    dynamicSupplier.get(clazz)
                }
            }
        }

        //The command object has to be created either by the instance supplier
        // or by the **only** constructor a class has
        // It must resolve all parameters types with the registered parameter suppliers
        val instanceSupplier = serviceContainer.context.serviceConfig.instanceSupplierMap[clazz]
        return when {
            instanceSupplier != null -> {
                measureTimedInstantiation {
                    instanceSupplier.supply(serviceContainer.context)
                        ?: throwService("Supplier function in class '${instanceSupplier::class.jvmName}' returned null")
                }
            }
            clazz.objectInstance != null -> measureTimedInstantiation { clazz.objectInstance }
            else -> {
                val constructingFunction = findConstructingFunction(clazz).getOrThrow()

                val timedInstantiation = constructingFunction.callConstructingFunction(serviceContainer)
                if (timedInstantiation.result.serviceError != null)
                    return timedInstantiation

                timedInstantiation
            }
        }.also { instance = it.result.getOrNull() }
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

    override fun toString() = providerKey
}

internal fun KClass<*>.getServiceName() = getAnnotatedServiceName() ?: this.simpleNestedName.replaceFirstChar { it.lowercase() }