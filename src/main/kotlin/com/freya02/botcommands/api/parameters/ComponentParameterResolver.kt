package com.freya02.botcommands.api.parameters

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.internal.components.ComponentDescriptor
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

/**
 * Interface which indicates this class can resolve parameters for buttons commands
 */
interface ComponentParameterResolver<T, R> where T : ParameterResolver<T, R>,
                                                 T : ComponentParameterResolver<T, R> {
    /**
     * Returns a resolved object from this component interaction
     *
     * @param context    The [BContext] of this bot
     * @param descriptor The component description of the component being executed
     * @param event      The event of this component interaction
     * @return The resolved option mapping
     */
    fun resolve(context: BContext, descriptor: ComponentDescriptor, event: GenericComponentInteractionCreateEvent, arg: String): R? =
        throw NotImplementedError("${this.javaClass.simpleName} must implement the 'resolve' or 'resolveSuspend' method")

    @JvmSynthetic
    suspend fun resolveSuspend(
        context: BContext,
        descriptor: ComponentDescriptor,
        event: GenericComponentInteractionCreateEvent,
        arg: String
    ) = resolve(context, descriptor, event, arg)
}