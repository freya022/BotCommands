package com.freya02.botcommands.api.core

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.core.annotations.ConditionalService

/**
 * One of the conditions required for a [ConditionalService] to be instantiated.
 *
 * **Requirement:** A no-arg constructor must exist, or must be a Kotlin `object`.
 *
 * @see ConditionalService
 */
interface ConditionalServiceChecker {
    /**
     * @return An error string if the service is not instantiable, `null` otherwise
     */
    fun checkServiceAvailability(context: BContext): String?
}