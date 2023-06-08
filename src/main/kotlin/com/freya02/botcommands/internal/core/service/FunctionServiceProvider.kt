package com.freya02.botcommands.internal.core.service

import com.freya02.botcommands.api.core.service.ServiceError
import com.freya02.botcommands.internal.utils.ReflectionUtils.shortSignatureNoSrc
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.jvmErasure

internal class FunctionServiceProvider(
    private val function: KFunction<*>,
    override var instance: Any? = null
) : ServiceProvider {
    override val name = function.getServiceName()
    override val providerKey = function.shortSignatureNoSrc
    override val primaryType get() = function.returnType.jvmErasure
    override val types = function.getServiceTypes(function.returnType.jvmErasure)

    private var isInstantiable = false

    override fun canInstantiate(serviceContainer: ServiceContainerImpl): ServiceError? {
        if (isInstantiable) return null
        if (instance != null) return null

        function.commonCanInstantiate(serviceContainer)?.let { serviceError -> return serviceError }
        function.checkConstructingFunction(serviceContainer)?.let { serviceError -> return serviceError }

        isInstantiable = true
        return null
    }

    override fun createInstance(serviceContainer: ServiceContainerImpl): TimedInstantiation {
        val timedInstantiation = function.callConstructingFunction(serviceContainer)
        if (timedInstantiation.result.serviceError != null)
            return timedInstantiation

        return timedInstantiation.also { instance = it.result.getOrThrow() }
    }

    override fun toString() = providerKey
}

internal fun KFunction<*>.getServiceName(): String = getAnnotatedServiceName() ?: this.name