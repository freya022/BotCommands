package io.github.freya022.botcommands.api.core.hooks.custom

import io.github.freya022.botcommands.api.core.hooks.custom.annotations.ExperimentalCustomEvents
import io.github.freya022.botcommands.api.core.utils.toEnumSet
import io.github.freya022.botcommands.internal.core.hooks.custom.CustomEventRequirementsImpl
import io.github.freya022.botcommands.internal.core.hooks.custom.EmptyCustomEventRequirements
import io.github.freya022.botcommands.internal.core.hooks.custom.UnknownCustomEventRequirements
import net.dv8tion.jda.api.requests.GatewayIntent

/**
 * Represents the requirements of a custom event.
 */
@ExperimentalCustomEvents
interface CustomEventRequirements {

    /**
     * Whether this custom event has no requirements.
     */
    fun isEmpty(): Boolean

    /**
     * Whether this custom event has unknown requirements.
     */
    fun areUnknown(): Boolean

    /**
     * The unmodifiable intent set required by this event. Can be empty.
     */
    fun getIntents(): Set<GatewayIntent>

    companion object {

        /**
         * Creates an instance from the given intents.
         */
        @JvmStatic
        fun from(intents: Array<GatewayIntent>): CustomEventRequirements {
            return from(intents.toEnumSet())
        }

        /**
         * Creates an instance from the given intents.
         */
        @JvmStatic
        fun from(intents: Collection<GatewayIntent>): CustomEventRequirements {
            if (intents.isEmpty())
                return none()

            return CustomEventRequirementsImpl(intents)
        }

        /**
         * Creates an instance indicating the event has no requirements.
         */
        @JvmStatic
        fun none(): CustomEventRequirements = EmptyCustomEventRequirements

        /**
         * Creates an instance indicating the event is unknown to the requirement provider.
         */
        @JvmStatic
        fun unknown(): CustomEventRequirements = UnknownCustomEventRequirements
    }
}
