package io.github.freya022.botcommands.api.core.service

import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.core.exceptions.ServiceException
import io.github.freya022.botcommands.internal.utils.shortSignature
import io.github.freya022.botcommands.internal.utils.throwInternal
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

class ServiceError private constructor(
    val errorType: ErrorType,
    val errorMessage: String,
    val nestedError: ServiceError?,
    val siblingErrors: List<ServiceError>,
    val extra: Map<String, Any>,
) {
    enum class ErrorType(val explanation: String) {
        UNKNOWN("Unknown service error"),
        DYNAMIC_NOT_INSTANTIABLE("Dynamic supplier could not create the service"),
        INVALID_CONSTRUCTING_FUNCTION("No valid constructor found"),
        NO_PROVIDER("No class annotated as a service or service factories were found"),
        NO_USABLE_PROVIDER("All providers returned errors"),
        NON_UNIQUE_PROVIDERS("Multiple providers were found but none were marked as primary"),
        INVALID_TYPE("The instantiated service was of the wrong type"),
        PROVIDER_RETURNED_NULL("The service provider returned no service"),
        UNAVAILABLE_DEPENDENCY("At least one dependency was missing"),
        FAILED_CONDITION("At least one check returned an error message"),
        UNAVAILABLE_INSTANCE("The instance required by a service factory was unavailable"),
        UNAVAILABLE_PARAMETER("At least one parameter from a constructor or a service factory was missing"),
        FAILED_CUSTOM_CONDITION("At least one custom check returned an error message"),
        FAILED_FATAL_CUSTOM_CONDITION("At least one custom check returned an error message, and was configured to fail");

        @JvmOverloads
        fun toError(errorMessage: String, extraMessage: String? = null, failedFunction: KFunction<*>? = null, nestedError: ServiceError? = null, siblingErrors: List<ServiceError> = emptyList(), extra: Map<String, Any> = emptyMap()) =
            ServiceError(this, errorMessage, nestedError, siblingErrors, buildMap(2) {
                if (extraMessage != null) put("Extra message", extraMessage)
                if (failedFunction != null) put("Failed function", failedFunction)
            } + extra)

        fun <T : Any> toResult(errorMessage: String, extraMessage: String? = null, failedFunction: KFunction<*>? = null, nestedError: ServiceError? = null, siblingErrors: List<ServiceError> = emptyList(), extra: Map<String, Any> = emptyMap()) =
            ServiceResult.fail<T>(toError(errorMessage, extraMessage, failedFunction, nestedError, siblingErrors, extra))
    }

    operator fun component1() = errorType
    operator fun component2() = errorMessage

    fun withErrorType(errorType: ErrorType): ServiceError =
        ServiceError(errorType, errorMessage, nestedError, siblingErrors, extra)

    fun withSibling(serviceError: ServiceError): ServiceError =
        ServiceError(errorType, errorMessage, nestedError, siblingErrors + serviceError, extra)

    fun withSiblings(serviceErrors: List<ServiceError>): ServiceError =
        ServiceError(errorType, errorMessage, nestedError, siblingErrors + serviceErrors, extra)

    fun toSimpleString(): String = when {
        siblingErrors.isEmpty() -> this.toSingleSimpleString()
        else -> (listOf(this) + siblingErrors).joinAsList { it.toSingleSimpleString() }
    }

    private fun toSingleSimpleString(): String = "$errorMessage (${errorType.explanation})"

    fun toDetailedString(): String = buildString {
        append(toSingleDetailedString())

        siblingErrors.forEach {
            appendLine()
            appendLine("See also:".prependIndent())
            append(it.toDetailedString().prependIndent())
        }
    }

    context(StringBuilder)
    internal fun appendPostfixSimpleString() {
        if (siblingErrors.isNotEmpty()) {
            append("\n")
            append(toSimpleString())
        } else {
            append(": ")
            append(toSimpleString())
        }
    }

    private fun toSingleDetailedString(): String = buildString {
        appendLine("    Error message: $errorMessage")
        forEachExtra { name, value ->
            appendLine("    $name: $value")
        }

        if (nestedError != null) {
            appendLine("Caused by:")
            appendLine(nestedError.toDetailedString())
        }
    }.trimEnd()

    private inline fun forEachExtra(block: (name: String, value: String) -> Unit) {
        extra.forEach { (k, v) ->
            val valueStr: String = when (v) {
                is KFunction<*> -> v.shortSignature
                is KClass<*> -> v.simpleNestedName
                is Lazy<*> -> v.value.toString()
                else -> v.toString()
            }
            block(k, valueStr)
        }
    }

    companion object {
        fun fromErrors(errors: List<ServiceError>): ServiceError {
            return errors.first().withSiblings(errors.drop(1))
        }
    }
}

class ServiceResult<out T : Any> private constructor(val service: T?, val serviceError: ServiceError?) {
    fun getOrNull(): T? = when {
        service != null -> service
        serviceError != null -> null
        else -> throwInternal("ServiceResult should contain either the service or the error message")
    }

    fun getOrThrow(): T = when {
        service != null -> service
        serviceError != null -> throw ServiceException(serviceError)
        else -> throwInternal("ServiceResult should contain either the service or the error message")
    }

    fun getOrThrow(block: (error: ServiceError) -> Nothing): T = when {
        service != null -> service
        serviceError != null -> block(serviceError)
        else -> throwInternal("ServiceResult should contain either the service or the error message")
    }

    inline fun onService(block: ServiceResult<T>.(service: T) -> Unit) = this.also { service?.also { block.invoke(this, it) } }
    inline fun onError(block: ServiceResult<T>.(serviceError: ServiceError) -> Unit) = this.also { serviceError?.also { block.invoke(this, it) } }

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
        fun <T : Any> fail(errors: List<ServiceError>) = fail<T>(ServiceError.fromErrors(errors))
    }
}