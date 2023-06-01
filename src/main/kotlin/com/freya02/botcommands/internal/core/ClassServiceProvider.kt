package com.freya02.botcommands.internal.core

import com.freya02.botcommands.api.core.annotations.InjectedService
import com.freya02.botcommands.internal.simpleNestedName
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmErasure

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
        val constructingFunction = serviceContainer.findConstructingFunction(clazz).let { it.getOrNull() ?: return it.errorMessage }
        constructingFunction.nonInstanceParameters.forEach {
            serviceContainer.canCreateService(it.type.jvmErasure)?.let { errorMessage -> return errorMessage }
        }

        return null
    }

    override fun createInstance(serviceContainer: ServiceContainerImpl): ServiceContainerImpl.TimedInstantiation {
        return serviceContainer.constructInstance(clazz).also { instance = it.result.getOrNull() }
    }

    override fun toString() = providerKey
}