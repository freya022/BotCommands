package com.freya02.botcommands.api.core.service

import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.throwService

class ServiceError private constructor(val errorType: ErrorType, val errorMessage: String, val additionalError: String?) {
    enum class ErrorType(val explanation: String) {
        DYNAMIC_NOT_INSTANTIABLE("Dynamic supplier could not create the service"),
        INVALID_CONSTRUCTING_FUNCTION("No valid constructor found"),
        NO_PROVIDER("No class annotated as a service or service factories were found"),
        INVALID_TYPE("The instantiated service was of the wrong type"),
        UNAVAILABLE_INJECTED_SERVICE("The injected service was not available at the time of instantiation"),
        UNAVAILABLE_DEPENDENCY("One or more dependencies were missing"),
        FAILED_CONDITION("One or more checks returned an error message");

        fun toError(errorMessage: String) = ServiceError(this, errorMessage, null)
        fun toError(errorMessage: String, additionalError: String) = ServiceError(this, errorMessage, additionalError)

        fun <T : Any> toResult(errorMessage: String) = ServiceResult.fail<T>(toError(errorMessage))
        fun <T : Any> toResult(errorMessage: String, additionalError: String) = ServiceResult.fail<T>(toError(errorMessage, additionalError))
    }

    operator fun component0() = errorType
    operator fun component1() = errorMessage
    operator fun component2() = additionalError

    override fun toString(): String = when {
        additionalError != null -> "$errorMessage (${errorType.explanation}, $additionalError)"
        else -> "$errorMessage (${errorType.explanation})"
    }
}

class ServiceResult<T : Any> private constructor(val service: T?, val serviceError: ServiceError?) {
    fun getOrNull(): T? = when {
        service != null -> service
        serviceError != null -> null
        else -> throwInternal("ServiceResult should contain either the service or the error message")
    }

    fun getOrThrow(): T = when {
        service != null -> service
        serviceError != null -> throwService(serviceError.errorMessage)
        else -> throwInternal("ServiceResult should contain either the service or the error message")
    }

    fun getOrThrow(block: (error: ServiceError) -> Nothing): T = when {
        service != null -> service
        serviceError != null -> block(serviceError)
        else -> throwInternal("ServiceResult should contain either the service or the error message")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ServiceResult<*>

        if (service != other.service) return false
        return serviceError == other.serviceError
    }

    override fun hashCode(): Int {
        var result = service?.hashCode() ?: 0
        result = 31 * result + (serviceError?.hashCode() ?: 0)
        return result
    }

    override fun toString() = when {
        service != null -> "ServiceResult[Pass](service=$service)"
        else -> "ServiceResult[Fail](serviceError=$serviceError)"
    }

    companion object {
        fun <T : Any> pass(service: T) = ServiceResult(service, null)
        fun <T : Any> fail(error: ServiceError) = ServiceResult<T>(null, error)
    }
}