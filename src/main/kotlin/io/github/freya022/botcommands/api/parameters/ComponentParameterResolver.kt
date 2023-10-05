package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.internal.components.ComponentDescriptor
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

/**
 * Interface which indicates this class can resolve parameters for component interactions.
 */
interface ComponentParameterResolver<T, R> where T : ParameterResolver<T, R>,
                                                 T : ComponentParameterResolver<T, R> {
    /**
     * Returns a resolved object from this component interaction
     *
     * @param descriptor The component description of the component being executed
     * @param event      The event of this component interaction
     * @return The resolved option mapping
     */
    fun resolve(descriptor: ComponentDescriptor, event: GenericComponentInteractionCreateEvent, arg: String): R? =
        throw NotImplementedError("${this.javaClass.simpleName} must implement the 'resolve' or 'resolveSuspend' method")

    @JvmSynthetic
    suspend fun resolveSuspend(
        descriptor: ComponentDescriptor,
        event: GenericComponentInteractionCreateEvent,
        arg: String
    ) = resolve(descriptor, event, arg)
}