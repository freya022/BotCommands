package io.github.freya022.botcommands.api.parameters.resolvers

import io.github.freya022.botcommands.api.core.Executable
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import net.dv8tion.jda.api.events.Event

/**
 * Parameter resolver for any command/handler parameters which aren't resolvable by other resolvers.
 *
 * Needs to be implemented alongside a [ParameterResolver] subclass.
 *
 * @param T Type of the implementation
 * @param R Type of the returned resolved objects
 */
interface ICustomResolver<T, R : Any> : IParameterResolver<T>
        where T : ParameterResolver<T, R>,
              T : ICustomResolver<T, R> {

    /**
     * Returns an object.
     *
     * The behavior when this returns `null` is the same as the "input" resolvers,
     * for example, [TextParameterResolver][TextParameterResolver.resolve],
     * [SlashParameterResolver][SlashParameterResolver.resolve] or
     * [ComponentParameterResolver][ComponentParameterResolver.resolve].
     *
     * @param executable Basic information about the function using this resolver,
     * may be any application command, text command, etc...
     * @param event      The event this resolver is called from
     */
    fun resolve(executable: Executable, event: Event): R? =
        throw NotImplementedError("${this.javaClass.simpleName} must implement the 'resolve' or 'resolveSuspend' method")

    /**
     * Returns an object.
     *
     * The behavior when this returns `null` is the same as the "input" resolvers,
     * for example, [TextParameterResolver][TextParameterResolver.resolveSuspend],
     * [SlashParameterResolver][SlashParameterResolver.resolveSuspend] or
     * [ComponentParameterResolver][ComponentParameterResolver.resolveSuspend].
     *
     * @param executable Basic information about the function using this resolver
     * @param event      The event this resolver is called from
     */
    @JvmSynthetic
    suspend fun resolveSuspend(executable: Executable, event: Event) =
        resolve(executable, event)
}