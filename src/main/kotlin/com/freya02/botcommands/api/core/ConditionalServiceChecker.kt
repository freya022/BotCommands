package com.freya02.botcommands.api.core

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.core.annotations.ConditionalService

/**
 * Kotlin: Implement this interface on the companion object
 *
 * Java: Implement this interface in a **static** inner class, with a no-arg constructor
 *
 * @see ConditionalService
 */
interface ConditionalServiceChecker {
    /**
     * @return An error string if the service is not instantiable, `null` otherwise
     */
    fun checkServiceAvailability(context: BContext): String?
}