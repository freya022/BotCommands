package io.github.freya022.botcommands.api.parameters.resolvers

import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.annotations.JDASelectMenuListener
import io.github.freya022.botcommands.api.components.builder.IPersistentActionableComponent
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.internal.components.handler.ComponentDescriptor
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import kotlin.reflect.KParameter
import kotlin.reflect.KType

/**
 * Parameter resolver for parameters of [@JDAButtonListener][JDAButtonListener]
 * and [@JDASelectMenuListener][JDASelectMenuListener].
 *
 * Needs to be implemented alongside a [ParameterResolver] subclass.
 *
 * @param T Type of the implementation
 * @param R Type of the returned resolved objects
 */
interface ComponentParameterResolver<T, R : Any> where T : ParameterResolver<T, R>,
                                                       T : ComponentParameterResolver<T, R> {
    /**
     * Returns a resolved object from this component interaction.
     *
     * If this returns `null`, and the parameter is required, i.e., not [nullable][KType.isMarkedNullable]
     * or [optional][KParameter.isOptional], then the handler aborts.
     *
     * The resolver should reply to the interaction in case the value is not resolvable.
     * If the interaction is not replied to, the handler throws.
     *
     * @param descriptor The descriptor of the component handler being executed
     * @param event      The corresponding event
     * @param arg        One of the data passed by the user in [IPersistentActionableComponent.bindTo]
     */
    fun resolve(descriptor: ComponentDescriptor, event: GenericComponentInteractionCreateEvent, arg: String): R? =
        throw NotImplementedError("${this.javaClass.simpleName} must implement the 'resolve' or 'resolveSuspend' method")

    /**
     * Returns a resolved object from this component interaction.
     *
     * If this returns `null`, and the parameter is required, i.e., not [nullable][KType.isMarkedNullable]
     * or [optional][KParameter.isOptional], then the handler aborts.
     *
     * The resolver should reply to the interaction in case the value is not resolvable.
     * If the interaction is not replied to, the handler throws.
     *
     * @param descriptor The descriptor of the component being executed
     * @param event      The corresponding event
     * @param arg        One of the data passed by the user in [IPersistentActionableComponent.bindTo]
     */
    @JvmSynthetic
    suspend fun resolveSuspend(
        descriptor: ComponentDescriptor,
        event: GenericComponentInteractionCreateEvent,
        arg: String
    ) = resolve(descriptor, event, arg)
}