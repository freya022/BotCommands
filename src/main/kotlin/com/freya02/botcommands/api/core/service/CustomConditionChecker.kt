package com.freya02.botcommands.api.core.service

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.core.service.annotations.Condition

/**
 * Defines custom conditions used while checking service instantiability.
 *
 * Whether the service creation failure throws or not, is defined by [Condition.fail],
 * or if a dependent service requires it.
 *
 * **Requirement:** A no-arg constructor must exist, or must be a Kotlin `object`.
 *
 * @see Condition @Condition
 */
interface CustomConditionChecker<A : Annotation> {
    val annotationType: Class<A>

    /**
     * @param context      The current BContext
     * @param checkedClass The primary type of the service being created,
     *                     the class being instantiated for services, or the return type for service factories
     * @param annotation   The condition annotation which triggered this check
     *
     * @return An error string if the service is not instantiable, `null` otherwise
     */
    fun checkServiceAvailability(context: BContext, checkedClass: Class<*>, annotation: A): String?
}