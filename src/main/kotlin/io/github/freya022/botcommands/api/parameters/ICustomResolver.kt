package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.internal.IExecutableInteractionInfo
import net.dv8tion.jda.api.events.Event
import kotlin.reflect.KParameter
import kotlin.reflect.KType

/**
 * Parameter resolver for any command/handler parameters which aren't resolvable by other resolvers.
 */
interface ICustomResolver<T, R : Any> where T : ParameterResolver<T, R>,
                                            T : ICustomResolver<T, R> {

    /**
     * Returns an object.
     *
     * If this returns `null`, and the parameter is required, i.e., not [nullable][KType.isMarkedNullable]
     * or [optional][KParameter.isOptional], then the handler will throw.
     *
     * @param executableInteractionInfo Basic information about the function using this resolver
     * @param event                     The event this resolver is called from
     */
    fun resolve(executableInteractionInfo: IExecutableInteractionInfo, event: Event): R? =
        throw NotImplementedError("${this.javaClass.simpleName} must implement the 'resolve' or 'resolveSuspend' method")

    /**
     * Returns an object.
     *
     * If this returns `null`, and the parameter is required, i.e., not [nullable][KType.isMarkedNullable]
     * or [optional][KParameter.isOptional], then the handler will throw.
     *
     * @param executableInteractionInfo Basic information about the function using this resolver
     * @param event                     The event this resolver is called from
     */
    @JvmSynthetic
    suspend fun resolveSuspend(executableInteractionInfo: IExecutableInteractionInfo, event: Event) =
        resolve(executableInteractionInfo, event)
}