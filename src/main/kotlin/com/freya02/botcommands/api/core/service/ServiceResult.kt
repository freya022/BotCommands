package com.freya02.botcommands.api.core.service

import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.throwService
import com.freya02.botcommands.internal.utils.ReflectionUtils.shortSignature
import kotlin.reflect.KFunction

class ServiceError private constructor(
    val errorType: ErrorType,
    val errorMessage: String,
    val extraMessage: String?,
    /**
     * This is to be handled by the code throwing the exception, as this is required to be on the first line
     */
    val failedFunction: KFunction<*>?,
    val nestedError: ServiceError?
) {
    enum class ErrorType(val explanation: String) {
        DYNAMIC_NOT_INSTANTIABLE("Dynamic supplier could not create the service"),
        INVALID_CONSTRUCTING_FUNCTION("No valid constructor found"),
        NO_PROVIDER("No class annotated as a service or service factories were found"),
        INVALID_TYPE("The instantiated service was of the wrong type"),
        UNAVAILABLE_INJECTED_SERVICE("The injected service was not available at the time of instantiation"),
        UNAVAILABLE_DEPENDENCY("At least one dependency were missing"),
        FAILED_CONDITION("At least one check returned an error message"),
        UNAVAILABLE_PARAMETER("At least one parameter from a constructor or a service factory was missing");

        @JvmOverloads
        fun toError(errorMessage: String, extraMessage: String? = null, failedFunction: KFunction<*>? = null, nestedError: ServiceError? = null) =
            ServiceError(this, errorMessage, extraMessage, failedFunction, nestedError)

        fun <T : Any> toResult(errorMessage: String, extraMessage: String? = null, failedFunction: KFunction<*>? = null, nestedError: ServiceError? = null) =
            ServiceResult.fail<T>(toError(errorMessage, extraMessage, failedFunction, nestedError))
    }

    operator fun component0() = errorType
    operator fun component1() = errorMessage
    operator fun component2() = extraMessage
    operator fun component3() = nestedError

    fun toSimpleString(): String = when {
        extraMessage != null -> "$errorMessage (${errorType.explanation}, $extraMessage)"
        else -> "$errorMessage (${errorType.explanation})"
    }

    fun toDetailedString(): String = buildString {
        appendLine("Error message: $errorMessage")
        if (failedFunction != null)
            appendLine("Failed function: ${failedFunction.shortSignature}")
        appendLine("Error type: ${errorType.explanation}")
        if (extraMessage != null)
            appendLine("Extra message: $extraMessage")

        if (nestedError != null) {
            val causedByHeader = " ".repeat(4) + "Caused by: "
            append(causedByHeader)

            val lines = nestedError.toDetailedString().trimIndent().lines()
            appendLine(lines.first())
            lines.drop(1).forEach {
                append(" ".repeat(causedByHeader.length))
                appendLine(it)
            }
        }
    }.prependIndent()
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
        else -> "ServiceResult[Fail](serviceError=${serviceError!!.toSimpleString()})"
    }

    companion object {
        fun <T : Any> pass(service: T) = ServiceResult(service, null)
        fun <T : Any> fail(error: ServiceError) = ServiceResult<T>(null, error)
    }
}