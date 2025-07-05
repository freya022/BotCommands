package io.github.freya022.botcommands.api.parameters.resolvers

import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.annotations.JDASelectMenuListener
import io.github.freya022.botcommands.api.components.builder.IPersistentActionableComponent
import io.github.freya022.botcommands.api.components.options.ComponentOption
import io.github.freya022.botcommands.api.components.serialization.SerializedComponentData
import io.github.freya022.botcommands.api.components.serialization.annotations.SerializableComponentData
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
 * ### Use case - Supporting serializable objects
 * If you need to pass **serializable** objects to your components,
 * you can instead use [@SerializableComponentData][SerializableComponentData]
 * and let it generate a resolver for you.
 *
 * @param T Type of the implementation
 * @param R Type of the returned resolved objects
 */
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
     * @param data   A serialized representation of an argument passed in [IPersistentActionableComponent.bindTo]
     */
    fun resolve(option: ComponentOption, event: GenericComponentInteractionCreateEvent, data: SerializedComponentData): R? =
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
     * @param data   A serialized representation of an argument passed in [IPersistentActionableComponent.bindTo]
     */
    @JvmSynthetic
    suspend fun resolveSuspend(option: ComponentOption, event: GenericComponentInteractionCreateEvent, data: SerializedComponentData) =
        resolve(option, event, data)

    /**
     * Serializes an instance of the resolvable object.
     *
     * The given instance can be serialized in any way you want,
     * remember you must be able to deserialize it in [resolve]/[resolveSuspend].
     */
    fun serialize(obj: R): SerializedComponentData
}