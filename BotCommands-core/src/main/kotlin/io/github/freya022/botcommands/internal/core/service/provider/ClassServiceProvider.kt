package io.github.freya022.botcommands.internal.core.service.provider

import io.github.freya022.botcommands.api.core.service.ServiceError
import io.github.freya022.botcommands.api.core.service.ServiceError.ErrorType
import io.github.freya022.botcommands.api.core.utils.getSignature
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.core.service.BCServiceContainerImpl
import io.github.freya022.botcommands.internal.utils.throwInternal
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KVisibility
import kotlin.reflect.jvm.jvmName

internal class ClassServiceProvider internal constructor(
    clazz: KClass<*>
) : AbstractClassServiceProvider(clazz) {
    init {
        require(!Modifier.isAbstract(clazz.java.modifiers) && !clazz.java.isInterface) {
            "Abstract class '${clazz.simpleNestedName}' cannot be constructed"
        }
    }

    override var instance: Any? = null
    /**
     * If not the sentinel value, the service was attempted to be created.
     */
    private var serviceError: ServiceError? = ServiceProvider.nullServiceError

    private val constructor = requireNotNull(clazz.constructors.singleOrNull()) {
        "Class ${clazz.simpleNestedName} must have exactly one constructor"
    }

    init {
        require(constructor.visibility == KVisibility.PUBLIC || constructor.visibility == KVisibility.INTERNAL) {
            "Constructor of ${clazz.simpleNestedName} must be public"
        }
    }

    override fun canInstantiate(serviceContainer: BCServiceContainerImpl): ServiceError? {
        // Returns null if there is no error, the error itself if there's one
        if (serviceError !== ServiceProvider.nullServiceError) return serviceError

        val serviceError = checkInstantiate(serviceContainer)
        //Do not cache service error if a parameter is unavailable, a retrial is allowed
        when (serviceError?.errorType) {
            ErrorType.UNAVAILABLE_PARAMETER, ErrorType.UNAVAILABLE_DEPENDENCY -> {}

            else -> this.serviceError = serviceError
        }

        return serviceError
    }

    private fun checkInstantiate(serviceContainer: BCServiceContainerImpl): ServiceError? {
        checkConditions(serviceContainer, clazz, clazz)?.let { serviceError -> return serviceError }

        //Check constructor parameters
        constructor.checkConstructingFunction(serviceContainer)?.let { serviceError -> return serviceError }

        return null
    }

    override fun createInstance(serviceContainer: BCServiceContainerImpl): TimedInstantiation<*> {
        if (instance != null)
            throwInternal("Tried to create an instance of ${clazz.jvmName} when one already exists, instance should be retrieved manually beforehand")

        // Definitely an error if an instance is trying to be created
        // before we know if it's instantiable.
        // We know it's instantiable when the error is null, throw if non-null
        serviceError?.let { serviceError ->
            throwInternal("""
                Tried to create an instance while a service error exists / hasn't been determined
                Provider: ${getProviderSignature()}
                Error: ${serviceError.toSimpleString()}
            """.trimIndent())
        }

        val timedInstantiation = createInstanceNonCached(serviceContainer)
        instance = timedInstantiation.instance
        return timedInstantiation
    }

    private fun createInstanceNonCached(serviceContainer: BCServiceContainerImpl): TimedInstantiation<*> {
        return constructor.callConstructingFunction(serviceContainer)
    }

    override fun getProviderFunction(): KFunction<*> {
        return constructor
    }

    override fun getProviderSignature(): String {
        return getProviderFunction().getSignature(parameters = false)
    }
}
