package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.core.reflect.ParameterWrapper

/**
 * Data used when retrieving a [resolver factory][ParameterResolverFactory].
 *
 * @param parameter    The parameter this resolver factory will be bound to
 * @param resolverData Contextual data which might be used to do further filtering in [ParameterResolverFactory.isResolvable]
 */
class ResolverRequest(
    val parameter: ParameterWrapper,
    val resolverData: ResolverData? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResolverRequest

        if (parameter != other.parameter) return false
        if (resolverData != other.resolverData) return false

        return true
    }

    override fun hashCode(): Int {
        var result = parameter.hashCode()
        result = 31 * result + (resolverData?.hashCode() ?: 0)
        return result
    }
}