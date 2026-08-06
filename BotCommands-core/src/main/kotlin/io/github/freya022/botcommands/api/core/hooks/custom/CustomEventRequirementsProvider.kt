package io.github.freya022.botcommands.api.core.hooks.custom

import io.github.freya022.botcommands.api.core.hooks.custom.annotations.ExperimentalCustomEvents
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService

/**
 * Provides the requirements for custom events.
 * At least one service must implement this interface if you have a listener for a custom event.
 *
 * Exactly one instance must return (known) requirements for a given event type.
 *
 * @see get
 */
@InterfacedService(acceptMultiple = true)
@ExperimentalCustomEvents
interface CustomEventRequirementsProvider {

    /**
     * Returns a set of requirements for the provided event type, the type corresponds to the listener's first parameter type.
     *
     * This function must return [CustomEventRequirements.unknown()][CustomEventRequirements.unknown] if the provided type
     * is not known (for example, if it comes from another module). Returning anything else in this case, is an error.
     *
     * @param handledEventType The type of event the listener can handle
     *
     * @return A [CustomEventRequirements] with the possible requirements
     */
    fun get(handledEventType: Class<*>): CustomEventRequirements
}
