package io.github.freya022.botcommands.api.parameters.resolvers

import io.github.freya022.botcommands.api.components.annotations.ComponentTimeoutHandler
import io.github.freya022.botcommands.api.components.annotations.GroupTimeoutHandler
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import kotlin.reflect.KParameter
import kotlin.reflect.KType

/**
 * Parameter resolver for parameters of [@ComponentTimeoutHandler][ComponentTimeoutHandler] and [@GroupTimeoutHandler][GroupTimeoutHandler].
 *
 * Needs to be implemented alongside a [ParameterResolver] subclass.
 *
 * @param T Type of the implementation
 * @param R Type of the returned resolved objects
 */
interface TimeoutParameterResolver<T, R : Any> : IParameterResolver<T>
        where T : ParameterResolver<T, R>,
              T : TimeoutParameterResolver<T, R> {

    /**
     * Returns a resolved object for this argument.
     *
     * If this returns `null`, and the parameter is required, i.e., not [nullable][KType.isMarkedNullable]
     * or [optional][KParameter.isOptional], the handler is ignored.
     *
     * @param arg The argument to be resolved
     */
    fun resolve(arg: String): R? =
        throw NotImplementedError("${this.javaClass.simpleName} must implement the 'resolve' or 'resolveSuspend' method")

    /**
     * Returns a resolved object for this argument.
     *
     * If this returns `null`, and the parameter is required, i.e., not [nullable][KType.isMarkedNullable]
     * or [optional][KParameter.isOptional], the handler is ignored.
     *
     * @param arg The argument to be resolved
     */
    @JvmSynthetic
    suspend fun resolveSuspend(arg: String): R? =
        resolve(arg)
}