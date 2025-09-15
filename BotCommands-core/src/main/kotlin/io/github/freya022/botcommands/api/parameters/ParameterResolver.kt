package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.parameters.resolvers.*

/**
 * Base class for parameter resolvers,
 * needs to be implemented alongside the interface of at least one interaction type:
 * - Text commands: [TextParameterResolver] or [QuotableTextParameterResolver]
 * - Slash commands: [SlashParameterResolver]
 * - Message context commands: [MessageContextParameterResolver]
 * - User context commands: [UserContextParameterResolver]
 * - Components: [ComponentParameterResolver]
 * - Component timeouts: [TimeoutParameterResolver]
 * - Modal handlers: [ModalParameterResolver]
 * - Custom parameter types: [ICustomResolver]
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
