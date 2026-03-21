package io.github.freya022.botcommands.internal.parameters

import io.github.freya022.botcommands.api.core.reflect.ParameterWrapper
import io.github.freya022.botcommands.api.parameters.ResolverData
import io.github.freya022.botcommands.api.parameters.ResolverRequest
import io.github.freya022.botcommands.api.parameters.resolvers.IParameterResolver

class TypedResolverRequest<T : IParameterResolver<T>>(
    override val resolverType: Class<T>,
    override val parameter: ParameterWrapper,
    override val resolverData: ResolverData? = null,
) : ResolverRequest {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TypedResolverRequest<*>) return false

        if (resolverType != other.resolverType) return false
        if (parameter != other.parameter) return false
        if (resolverData != other.resolverData) return false

        return true
    }

    override fun hashCode(): Int {
        var result = resolverType.hashCode()
        result = 31 * result + parameter.hashCode()
        result = 31 * result + (resolverData?.hashCode() ?: 0)
        return result
    }
}
