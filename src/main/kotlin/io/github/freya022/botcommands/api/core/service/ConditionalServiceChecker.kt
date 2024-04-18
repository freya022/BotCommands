package io.github.freya022.botcommands.api.core.service

import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService

/**
 * One of the conditions required for a [ConditionalService] to be instantiated.
 *
 * **Requirement:** A no-arg constructor must exist, or must be a Kotlin `object`.
 *
 * @see ConditionalService @ConditionalService
 */
interface ConditionalServiceChecker {
    /**
     * @param serviceContainer The service container for this context
     * @param checkedClass     The primary type of the service being created,
     *                         the class being instantiated for services, or the return type for service factories
     *
     * @return An error string if the service is not instantiable, `null` otherwise
     */
    fun checkServiceAvailability(serviceContainer: ServiceContainer, checkedClass: Class<*>): String?
}