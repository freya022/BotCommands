package com.freya02.botcommands.api.core.service

import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.throwService

class ServiceResult<T : Any> private constructor(val service: T?, val errorMessage: String?) {
    fun getOrNull(): T? = when {
        service != null -> service
        errorMessage != null -> null
        else -> throwInternal("ServiceResult should contain either the service or the error message")
    }

    fun getOrThrow(): T = when {
        service != null -> service
        errorMessage != null -> throwService(errorMessage)
        else -> throwInternal("ServiceResult should contain either the service or the error message")
    }

    fun getOrThrow(block: (errorMessage: String) -> Nothing): T = when {
        service != null -> service
        errorMessage != null -> block(errorMessage)
        else -> throwInternal("ServiceResult should contain either the service or the error message")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ServiceResult<*>

        if (service != other.service) return false
        return errorMessage == other.errorMessage
    }

    override fun hashCode(): Int {
        var result = service?.hashCode() ?: 0
        result = 31 * result + (errorMessage?.hashCode() ?: 0)
        return result
    }

    override fun toString() = when {
        service != null -> "ServiceResult[Pass](service=$service)"
        else -> "ServiceResult[Fail](errorMessage=$errorMessage)"
    }

    companion object {
        fun <T : Any> pass(service: T) = ServiceResult(service, null)
        fun <T : Any> fail(errorMessage: String) = ServiceResult<T>(null, errorMessage)
    }
}