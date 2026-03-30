package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.parameters.resolvers.IParameterResolver

/**
 * Base class for parameter resolvers.
 *
 * Parameter resolvers for services exist by default, and follow the rules described in [@BService][BService].
 *
 * You can also check loaded parameter resolvers in the logs on the `trace` level.
 *
 * ### Usage
 * As this class is sealed, you need to extend [ClassParameterResolver] or [TypedParameterResolver] instead.
 *
 * @param T Type of the implementation
 * @param R Type of the returned resolved objects
 *
 * @see ParameterResolverFactory
 *
 * @see Resolvers
 */
@InterfacedService(acceptMultiple = true)
sealed class ParameterResolver<T : ParameterResolver<T, R>, R : Any> : IParameterResolver<T>
