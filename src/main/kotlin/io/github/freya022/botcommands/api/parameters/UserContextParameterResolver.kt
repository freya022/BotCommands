package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAUserCommand
import io.github.freya022.botcommands.internal.commands.application.context.user.UserCommandInfo
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import kotlin.reflect.KParameter
import kotlin.reflect.KType

/**
 * Parameter resolver for parameters of [@JDAUserCommand][JDAUserCommand].
 */
interface UserContextParameterResolver<T, R : Any> where T : ParameterResolver<T, R>,
                                                         T : UserContextParameterResolver<T, R> {
    /**
     * Returns a resolved object from this user context interaction.
     *
     * If this returns `null`, and the parameter is required, i.e., not [nullable][KType.isMarkedNullable]
     * or [optional][KParameter.isOptional], then the handler will throw.
     *
     * @param info  The data of the command being executed
     * @param event The corresponding event
     */
    fun resolve(info: UserCommandInfo, event: UserContextInteractionEvent): R? =
        throw NotImplementedError("${this.javaClass.simpleName} must implement the 'resolve' or 'resolveSuspend' method")

    /**
     * Returns a resolved object from this user context interaction.
     *
     * If this returns `null`, and the parameter is required, i.e., not [nullable][KType.isMarkedNullable]
     * or [optional][KParameter.isOptional], then the handler will throw.
     *
     * @param info  The data of the command being executed
     * @param event The corresponding event
     */
    @JvmSynthetic
    suspend fun resolveSuspend(info: UserCommandInfo, event: UserContextInteractionEvent) =
        resolve(info, event)
}