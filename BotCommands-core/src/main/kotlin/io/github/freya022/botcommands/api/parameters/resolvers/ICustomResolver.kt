package io.github.freya022.botcommands.api.parameters.resolvers

import io.github.freya022.botcommands.api.core.options.Option
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.internal.parameters.resolvers.ResolverMarker
import net.dv8tion.jda.api.events.Event

/**
 * Parameter resolver for any command/handler parameters which aren't resolvable by other resolvers.
 *
 * Needs to be implemented alongside a [ParameterResolver] subclass.
 *
 * @param T Type of the implementation
 * @param R Type of the returned resolved objects
 */
interface ICustomResolver<T, R : Any> : IParameterResolver<T>, ResolverMarker
        where T : ParameterResolver<T, R>,
              T : ICustomResolver<T, R> {

    /**
     * Returns an object.
     *
     * The behavior when this returns `null` is the same as the "input" resolvers.
     *
     * @param option The option currently being resolved, may be from an application command, text command, etc...
     * @param event  The event triggering this resolver
     */
    fun resolve(option: Option, event: Event): R? =
        throw NotImplementedError("${this.javaClass.simpleName} must implement the 'resolve' or 'resolveSuspend' method")

    /**
     * Returns an object.
     *
     * The behavior when this returns `null` is the same as the "input" resolvers.
     *
     * @param option The option currently being resolved, may be from an application command, text command, etc...
     * @param event  The event triggering this resolver
     */
    @JvmSynthetic
    suspend fun resolveSuspend(option: Option, event: Event) =
        resolve(option, event)
}
