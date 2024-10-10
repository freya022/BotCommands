package io.github.freya022.botcommands.api.parameters.resolvers

import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAUserCommand
import io.github.freya022.botcommands.api.commands.application.context.user.UserCommandInfo
import io.github.freya022.botcommands.api.commands.application.context.user.options.UserContextCommandOption
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import kotlin.reflect.KParameter
import kotlin.reflect.KType

/**
 * Parameter resolver for parameters of [@JDAUserCommand][JDAUserCommand].
 *
 * Needs to be implemented alongside a [ParameterResolver] subclass.
 *
 * @param T Type of the implementation
 * @param R Type of the returned resolved objects
 */
@Suppress("DEPRECATION", "DeprecatedCallableAddReplaceWith")
interface UserContextParameterResolver<T, R : Any> : IParameterResolver<T>
        where T : ParameterResolver<T, R>,
              T : UserContextParameterResolver<T, R> {

    /**
     * Returns a resolved object from this user context interaction.
     *
     * If this returns `null`, and the parameter is required, i.e., not [nullable][KType.isMarkedNullable]
     * or [optional][KParameter.isOptional], then the handler will throw.
     *
     * @param option The option currently being resolved
     * @param event  The corresponding event
     */
    fun resolve(option: UserContextCommandOption, event: UserContextInteractionEvent): R? =
        resolve(option.executable, event)

    @Deprecated("Replaced UserCommandInfo with UserContextCommandOption")
    fun resolve(info: UserCommandInfo, event: UserContextInteractionEvent): R? =
        throw NotImplementedError("${this.javaClass.simpleName} must implement the 'resolve' or 'resolveSuspend' method")

    /**
     * Returns a resolved object from this user context interaction.
     *
     * If this returns `null`, and the parameter is required, i.e., not [nullable][KType.isMarkedNullable]
     * or [optional][KParameter.isOptional], then the handler will throw.
     *
     * @param option The option currently being resolved
     * @param event  The corresponding event
     */
    @JvmSynthetic
    suspend fun resolveSuspend(option: UserContextCommandOption, event: UserContextInteractionEvent) =
        resolve(option, event)

    @Deprecated("Replaced UserCommandInfo with UserContextCommandOption")
    @JvmSynthetic
    suspend fun resolveSuspend(info: UserCommandInfo, event: UserContextInteractionEvent) =
        resolve(info, event)
}