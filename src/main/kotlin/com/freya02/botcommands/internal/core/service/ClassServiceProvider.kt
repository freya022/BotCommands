package com.freya02.botcommands.internal.core.service

import com.freya02.botcommands.api.core.service.DynamicSupplier
import com.freya02.botcommands.api.core.service.DynamicSupplier.Instantiability.InstantiabilityType
import com.freya02.botcommands.api.core.service.ServiceError
import com.freya02.botcommands.api.core.service.ServiceError.ErrorType
import com.freya02.botcommands.api.core.service.ServiceResult
import com.freya02.botcommands.api.core.service.annotations.InjectedService
import com.freya02.botcommands.api.core.service.getInterfacedServices
import com.freya02.botcommands.internal.simpleNestedName
import com.freya02.botcommands.internal.throwService
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
    override val providerKey = clazz.simpleNestedName
    override val primaryType get() = clazz
    override val types = clazz.getServiceTypes(clazz)

    private var isInstantiable = false

    override fun canInstantiate(serviceContainer: ServiceContainerImpl): ServiceError? {
        if (isInstantiable) return null
        if (instance != null) return null

        clazz.findAnnotation<InjectedService>()?.let {
            //Skips cache
            return ErrorType.UNAVAILABLE_INJECTED_SERVICE.toError("Tried to load an unavailable InjectedService '${clazz.simpleNestedName}', reason might include: ${it.message}")
        }

        clazz.commonCanInstantiate(serviceContainer, clazz)?.let { serviceError -> return serviceError }

        //Check dynamic suppliers
        serviceContainer.getInterfacedServices<DynamicSupplier>().forEach { dynamicSupplier ->
            val instantiability = dynamicSupplier.getInstantiability(serviceContainer.context, clazz)
            when (instantiability.type) {
                //Return error message
                InstantiabilityType.NOT_INSTANTIABLE -> return ErrorType.DYNAMIC_NOT_INSTANTIABLE.toError(instantiability.message!!, "${dynamicSupplier::class.simpleNestedName} failed")
                //Continue looking at other suppliers
                InstantiabilityType.UNSUPPORTED_TYPE -> {}
                //Found a supplier, return no error message
                InstantiabilityType.INSTANTIABLE -> {
                    isInstantiable = true
                    return null
                }
            }
        }

        //Is a singleton
        if (clazz.objectInstance != null) {
            isInstantiable = true
            return null
        }

        //Check constructor parameters
        //It's fine if there's no constructor, it just means it's not instantiable
        val constructingFunction = findConstructingFunction(clazz).let { it.getOrNull() ?: return it.serviceError }
        constructingFunction.checkConstructingFunction(serviceContainer)?.let { serviceError -> return serviceError }

        isInstantiable = true
        return null
    }

    override fun createInstance(serviceContainer: ServiceContainerImpl): TimedInstantiation {
        serviceContainer.getInterfacedServices<DynamicSupplier>().forEach { dynamicSupplier ->
            val instantiability = dynamicSupplier.getInstantiability(serviceContainer.context, clazz)
            when (instantiability.type) {
                //Return error message
                InstantiabilityType.NOT_INSTANTIABLE -> ErrorType.DYNAMIC_NOT_INSTANTIABLE.toResult<Any>(instantiability.message!!, "${dynamicSupplier::class.simpleNestedName} failed")
                    .toFailedTimedInstantiation()
                //Continue looking at other suppliers
                InstantiabilityType.UNSUPPORTED_TYPE -> {}
                //Found a supplier, return instance
                InstantiabilityType.INSTANTIABLE -> return measureTimedInstantiation {
                    dynamicSupplier.get(serviceContainer.context, clazz)
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