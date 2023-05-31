package com.freya02.botcommands.internal.core

import com.freya02.botcommands.api.core.ConditionalServiceChecker
import com.freya02.botcommands.api.core.annotations.ConditionalService
import com.freya02.botcommands.api.core.annotations.Dependencies
import com.freya02.botcommands.api.core.annotations.InjectedService
import com.freya02.botcommands.internal.requireUser
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

        clazz.findAnnotation<Dependencies>()?.value?.let { dependencies ->
            dependencies.forEach { dependency ->
                serviceContainer.canCreateService(dependency)?.let { errorMessage ->
                    return "Conditional service depends on ${dependency.simpleNestedName} but it is not available: $errorMessage"
                }
            }
        }

        // Services can be conditional
        // They can implement an interface to do checks
        // They can also depend on other services, in which case the interface becomes optional
        clazz.findAnnotation<ConditionalService>()?.let { conditionalService ->
            val checker = serviceContainer.findConditionalServiceChecker(clazz)
            requireUser(checker != null) {
                "Conditional service ${clazz.simpleNestedName} needs to implement ${ConditionalServiceChecker::class.simpleNestedName}, check the docs for more details"
            }
            checker.checkServiceAvailability(serviceContainer.context)?.let { errorMessage -> return errorMessage } //Final optional check
        }

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