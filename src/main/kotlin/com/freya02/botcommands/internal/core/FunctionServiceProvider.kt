package com.freya02.botcommands.internal.core

import com.freya02.botcommands.api.core.ServiceResult
import com.freya02.botcommands.api.core.annotations.Dependencies
import com.freya02.botcommands.internal.core.ServiceContainerImpl.Companion.callStatic
import com.freya02.botcommands.internal.simpleNestedName
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import com.freya02.botcommands.internal.utils.ReflectionUtils.shortSignatureNoSrc
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmErasure
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

internal class FunctionServiceProvider(private val function: KFunction<*>, override var instance: Any? = null) : ServiceProvider {
    override val name = function.getServiceName()
    override val providerKey = function.shortSignatureNoSrc
    override val primaryType get() = function.returnType.jvmErasure
    override val types = function.getServiceTypes(function.returnType.jvmErasure)

    override fun canInstantiate(serviceContainer: ServiceContainerImpl): String? {
        if (instance != null) return null

        function.findAnnotation<Dependencies>()?.value?.let { dependencies ->
            dependencies.forEach { dependency ->
                serviceContainer.canCreateService(dependency)?.let { errorMessage ->
                    return "Conditional service depends on ${dependency.simpleNestedName} but it is not available: $errorMessage"
                }
            }
        }

        return null
    }

    @OptIn(ExperimentalTime::class)
    override fun createInstance(serviceContainer: ServiceContainerImpl): ServiceContainerImpl.TimedInstantiation {
        val params = function.nonInstanceParameters.map {
            val dependencyResult = serviceContainer.tryGetService(it.type.jvmErasure) //Try to get a dependency, if it doesn't work then return the message
            //TODO make ServiceResult.toFailedTimedInstantation extension
            dependencyResult.service ?: return ServiceContainerImpl.TimedInstantiation(
                ServiceResult.fail<Any>(dependencyResult.errorMessage!!), Duration.INFINITE
            )
        }
        return measureTimedValue { function.callStatic(*params.toTypedArray()) } //Avoid measuring time it takes to load other services
            .also { instance = it.value }
            .let { ServiceContainerImpl.TimedInstantiation(ServiceResult.pass(it.value!!), it.duration) }
    }

    override fun toString() = providerKey
}