package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.core.reflect.ParameterWrapper
import io.github.freya022.botcommands.api.parameters.resolvers.IParameterResolver

/**
 * Data used when retrieving a [resolver factory][ParameterResolverFactory].
 */
interface ResolverRequest {

    /**
     * The type of built-in resolver being requested
     */
    val resolverType: Class<out IParameterResolver<*>>

    /**
     * The parameter this resolver factory will be bound to
     */
    val parameter: ParameterWrapper

    /**
     * Contextual data which might be used to do further filtering in [ParameterResolverFactory.isResolvable]
     */
    val resolverData: ResolverData?

    abstract override fun equals(other: Any?): Boolean

    abstract override fun hashCode(): Int
}
