package io.github.freya022.botcommands.api.parameters.resolvers

import io.github.freya022.botcommands.api.commands.application.context.message.options.MessageContextCommandOption
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import kotlin.reflect.KParameter
import kotlin.reflect.KType

/**
 * Resolver for parameters of message context commands.
 *
 * Needs to be implemented alongside a [ParameterResolver] subclass.
 *
 * ### Types supported by default
 * - [Message]
 *
 * @param T Type of the implementation
 * @param R Type of the returned resolved objects
 */
interface MessageContextParameterResolver<T, R : Any> : IParameterResolver<T>
        where T : ParameterResolver<T, R>,
              T : MessageContextParameterResolver<T, R> {

    /**
     * Returns a resolved object from this message context interaction.
     *
     * If this returns `null`, and the parameter is required, i.e., not [nullable][KType.isMarkedNullable]
     * or [optional][KParameter.isOptional], then the handler will throw.
     *
     * @param option The option currently being resolved
     * @param event  The corresponding event
     */
    fun resolve(option: MessageContextCommandOption, event: MessageContextInteractionEvent): R? =
        throw NotImplementedError("${this.javaClass.simpleName} must implement the 'resolve' or 'resolveSuspend' method")

    /**
     * Returns a resolved object from this message context interaction.
     *
     * If this returns `null`, and the parameter is required, i.e., not [nullable][KType.isMarkedNullable]
     * or [optional][KParameter.isOptional], then the handler will throw.
     *
     * @param option The option currently being resolved
     * @param event  The corresponding event
     */
    @JvmSynthetic
    suspend fun resolveSuspend(option: MessageContextCommandOption, event: MessageContextInteractionEvent) =
        resolve(option, event)
}
