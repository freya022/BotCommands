package io.github.freya022.botcommands.api.core.hooks.custom

import io.github.freya022.botcommands.api.core.hooks.custom.annotations.ExperimentalCustomEvents
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService

/**
 * Provides the requirements for custom events.
 * At least one service must implement this interface if you have a listener for a custom event.
 *
 * Exactly one instance must return (known) requirements for a given event type.
 *
 * ### Example
 *
 * ```java
 * @BService
 * @NullMarked
 * public class MyCustomEventRequirementsProvider implements CustomEventRequirementsProvider {
 *     // A custom event indicating a member moved to a different audio channel
 *     static class GuildVoiceChannelMoveEvent {
 *         // ...
 *     }
 *
 *     @Override
 *     public CustomEventRequirements get(Class<?> handledEventType) {
 *         // Any event that is or extends GuildVoiceChannelMoveEvent
 *         if (GuildVoiceChannelMoveEvent.class.isAssignableFrom(handledEventType)) {
 *             // A member moving from a channel to another is signaled by a GuildVoiceUpdateEvent,
 *             //  so we give the same requirements
 *             return CustomEventRequirements.fromEvents(GuildVoiceUpdateEvent.class);
 *         }
 *
 *         // For other events than those we directly support.
 *         // It can also be events from a different module,
 *         //  in which case that module should have a CustomEventRequirementsProvider too.
 *         return CustomEventRequirements.unknown();
 *     }
 * }
 * ```
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
