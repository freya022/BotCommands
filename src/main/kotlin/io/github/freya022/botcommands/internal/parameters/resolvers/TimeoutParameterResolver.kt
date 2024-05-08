package io.github.freya022.botcommands.internal.parameters.resolvers

import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.IParameterResolver
import io.github.freya022.botcommands.internal.components.timeout.TimeoutDescriptor

internal interface TimeoutParameterResolver<T, R : Any> : IParameterResolver<T>
        where T : ParameterResolver<T, R>,
              T : TimeoutParameterResolver<T, R> {

    fun resolve(descriptor: TimeoutDescriptor<*>, arg: String): R? =
        throw NotImplementedError("${this.javaClass.simpleName} must implement the 'resolve' or 'resolveSuspend' method")

    @JvmSynthetic
    suspend fun resolveSuspend(descriptor: TimeoutDescriptor<*>, arg: String): R? =
        resolve(descriptor, arg)
}