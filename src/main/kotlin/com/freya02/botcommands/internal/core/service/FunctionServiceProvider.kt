package com.freya02.botcommands.internal.core.service

import com.freya02.botcommands.api.core.service.ServiceError
import com.freya02.botcommands.api.core.service.ServiceError.ErrorType
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.internal.bestName
import com.freya02.botcommands.internal.simpleNestedName
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import com.freya02.botcommands.internal.utils.ReflectionUtils.shortSignature
import com.freya02.botcommands.internal.utils.ReflectionUtils.shortSignatureNoSrc
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmErasure

internal class FunctionServiceProvider(
    private val function: KFunction<*>,
    override var instance: Any? = null
) : ServiceProvider {
    override val name = function.getServiceName()
    override val providerKey = function.shortSignatureNoSrc
    override val primaryType get() = function.returnType.jvmErasure
    override val types = function.getServiceTypes(function.returnType.jvmErasure)

    override fun canInstantiate(serviceContainer: ServiceContainerImpl): ServiceError? {
        if (instance != null) return null

        function.commonCanInstantiate(serviceContainer)?.let { serviceError -> return serviceError }

        function.nonInstanceParameters.forEach {
            serviceContainer.canCreateService(it.type.jvmErasure)?.let { serviceError ->
                return ErrorType.UNAVAILABLE_PARAMETER.toError(
                    "Cannot get service for parameter '${it.bestName}' (${it.type.jvmErasure.simpleNestedName}) in ${function.shortSignature}",
                    nestedError = serviceError
                )
            }
        }

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

internal fun KFunction<*>.getServiceName(annotation: BService? = this.findAnnotation()): String = when {
    annotation == null || annotation.name.isEmpty() -> this.name
    else -> annotation.name
}