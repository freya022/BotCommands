package com.freya02.botcommands.internal.core

import com.freya02.botcommands.api.core.ServiceResult
import com.freya02.botcommands.api.core.annotations.InjectedService
import com.freya02.botcommands.internal.simpleNestedName
import com.freya02.botcommands.internal.throwService
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KVisibility
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

internal class ClassServiceProvider(private val clazz: KClass<*>, override var instance: Any? = null) : ServiceProvider {
    override val name = clazz.getServiceName()
    override val providerKey = clazz.simpleNestedName
    override val primaryType get() = clazz
    override val types = clazz.getServiceTypes(clazz)

    override fun canInstantiate(serviceContainer: ServiceContainerImpl): String? {
        if (instance != null) return null

        clazz.findAnnotation<InjectedService>()?.let {
            //Skips cache
            return "Tried to load an unavailable InjectedService '${clazz.simpleNestedName}', reason might include: ${it.message}"
        }

        clazz.commonCanInstantiate(serviceContainer)?.let { errorMessage -> return errorMessage }

        //Check parameters of dynamic resolvers
        serviceContainer.dynamicSuppliers.forEach { dynamicSupplierFunction ->
            dynamicSupplierFunction.nonInstanceParameters.drop(1).forEach {
                serviceContainer.canCreateService(it.type.jvmErasure)?.let { errorMessage -> return errorMessage }
            }
        }

        //Is a singleton
        if (clazz.objectInstance != null) return null

        //Check constructor parameters
        //It's fine if there's no constructor, it just means it's not instantiable
        val constructingFunction = findConstructingFunction(clazz).let { it.getOrNull() ?: return it.errorMessage }
        constructingFunction.nonInstanceParameters.forEach {
            serviceContainer.canCreateService(it.type.jvmErasure)?.let { errorMessage -> return errorMessage }
        }

        return null
    }

    @OptIn(ExperimentalTime::class)
    override fun createInstance(serviceContainer: ServiceContainerImpl): ServiceContainerImpl.TimedInstantiation {
        serviceContainer.dynamicSuppliers.forEach { dynamicSupplierFunction ->
            measureTimedValue {
                runDynamicSupplier(serviceContainer, clazz, dynamicSupplierFunction)
            }.toTimedInstantiationOrNull()?.let { return it }
        }

        //The command object has to be created either by the instance supplier
        // or by the **only** constructor a class has
        // It must resolve all parameters types with the registered parameter suppliers
        val instanceSupplier = serviceContainer.context.serviceConfig.instanceSupplierMap[clazz]
        return when {
            instanceSupplier != null -> {
                measureTimedValue {
                    instanceSupplier.supply(serviceContainer.context)
                        ?: throwService("Supplier function in class '${instanceSupplier::class.jvmName}' returned null")
                }
            }
            clazz.objectInstance != null -> measureTimedValue { clazz.objectInstance }
            else -> {
                val constructingFunction = findConstructingFunction(clazz).getOrThrow()

                val params = constructingFunction.nonInstanceParameters.map {
                    val dependencyResult = serviceContainer.tryGetService(it.type.jvmErasure)
                    //Try to get a dependency, if it doesn't work then return the message
                    dependencyResult.service ?: return dependencyResult.toFailedTimedInstantiation()
                }
                measureTimedValue { constructingFunction.callStatic(*params.toTypedArray()) } //Avoid measuring time it takes to load other services
            }
        }.toTimedInstantiation().also { instance = it.result.getOrNull() }
    }

    private fun findConstructingFunction(clazz: KClass<*>): ServiceResult<KFunction<*>> {
        val constructors = clazz.constructors
        if (constructors.isEmpty())
            return ServiceResult.fail("Class ${clazz.simpleNestedName} must have an accessible constructor")
        if (constructors.size != 1)
            return ServiceResult.fail("Class ${clazz.simpleNestedName} must have exactly one constructor")

        val constructor = constructors.single()
        if (constructor.visibility != KVisibility.PUBLIC && constructor.visibility != KVisibility.INTERNAL) {
            return ServiceResult.fail("Constructor of ${clazz.simpleNestedName} must be public")
        }

        return ServiceResult.pass(constructor)
    }

    private fun runDynamicSupplier(serviceContainer: ServiceContainerImpl, requestedType: KClass<*>, dynamicSupplierFunction: KFunction<*>): Any? {
        val params: List<Any> = dynamicSupplierFunction.nonInstanceParameters.drop(1).map {
            //Try to get a dependency, if it doesn't work then skip this supplier
            serviceContainer.tryGetService(it.type.jvmErasure).service ?: return null
        }

        return dynamicSupplierFunction.callStatic(requestedType.java, *params.toTypedArray())
    }

    override fun toString() = providerKey
}