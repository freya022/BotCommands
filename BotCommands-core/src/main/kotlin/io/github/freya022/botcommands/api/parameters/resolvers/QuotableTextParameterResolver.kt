package io.github.freya022.botcommands.api.parameters.resolvers

import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.internal.parameters.resolvers.ResolverMarker
import java.util.regex.Pattern

/**
 * Resolver for text command options.
 *
 * This resolver is an extension of [TextParameterResolver], but the regex pattern is quoted to help command parsing.
 *
 * Needs to be implemented alongside a [ParameterResolver] subclass.
 *
 * @param T Type of the implementation
 * @param R Type of the returned resolved objects
 */
interface QuotableTextParameterResolver<T, R : Any> : TextParameterResolver<T, R>, ResolverMarker
        where T : ParameterResolver<T, R>,
              T : QuotableTextParameterResolver<T, R> {
    /**
     * A quoted pattern of the parameter resolver.
     *
     * @see TextParameterResolver.pattern
     */
    val quotedPattern: Pattern

    override val preferredPattern: Pattern
        get() = quotedPattern
}
