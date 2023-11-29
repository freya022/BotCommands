package io.github.freya022.botcommands.internal.parameters.resolvers

import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.internal.components.timeout.TimeoutDescriptor

internal interface TimeoutParameterResolver<T, R : Any> where T : ParameterResolver<T, R>,
                                                              T : TimeoutParameterResolver<T, R> {
    fun resolve(descriptor: TimeoutDescriptor<*>, arg: String): R
}