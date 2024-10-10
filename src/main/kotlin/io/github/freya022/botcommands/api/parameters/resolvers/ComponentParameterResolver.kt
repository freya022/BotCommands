package io.github.freya022.botcommands.api.parameters.resolvers

import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.annotations.JDASelectMenuListener
import io.github.freya022.botcommands.api.components.builder.IPersistentActionableComponent
import io.github.freya022.botcommands.api.components.options.ComponentOption
import io.github.freya022.botcommands.api.parameters.ParameterResolver
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
@Suppress("DEPRECATION", "DeprecatedCallableAddReplaceWith")
interface ComponentParameterResolver<T, R : Any> : IParameterResolver<T>
        where T : ParameterResolver<T, R>,
              T : ComponentParameterResolver<T, R> {

    /**
     * Returns a resolved object from this component interaction.
     *
     * If this returns `null`, and the parameter is required, i.e., not [nullable][KType.isMarkedNullable]
     * or [optional][KParameter.isOptional], the handler is ignored,
     * but the interaction **must** be acknowledged.
     *
     * @param option The option currently being resolved
     * @param event  The corresponding event
     * @param arg    One of the data passed by the user in [IPersistentActionableComponent.bindTo]
     */
    fun resolve(option: ComponentOption, event: GenericComponentInteractionCreateEvent, arg: String): R? =
        resolve(event, arg)

    @Deprecated("Added a ComponentOption parameter")
    fun resolve(event: GenericComponentInteractionCreateEvent, arg: String): R? =
        throw NotImplementedError("${this.javaClass.simpleName} must implement the 'resolve' or 'resolveSuspend' method")

    /**
     * Returns a resolved object from this component interaction.
     *
     * If this returns `null`, and the parameter is required, i.e., not [nullable][KType.isMarkedNullable]
     * or [optional][KParameter.isOptional], the handler is ignored,
     * but the interaction **must** be acknowledged.
     *
     * @param option The option currently being resolved
     * @param event  The corresponding event
     * @param arg    One of the data passed by the user in [IPersistentActionableComponent.bindTo]
     */
    @JvmSynthetic
    suspend fun resolveSuspend(option: ComponentOption, event: GenericComponentInteractionCreateEvent, arg: String) =
        resolve(option, event, arg)

    @Deprecated("Added a ComponentOption parameter")
    @JvmSynthetic
    suspend fun resolveSuspend(event: GenericComponentInteractionCreateEvent, arg: String) =
        resolve(event, arg)
}