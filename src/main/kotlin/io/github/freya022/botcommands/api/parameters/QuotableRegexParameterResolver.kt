package io.github.freya022.botcommands.api.parameters

import java.util.regex.Pattern

/**
 * Interface which indicates this class can resolve parameters for regex commands.
 */
interface QuotableRegexParameterResolver<T, R : Any> : RegexParameterResolver<T, R>
        where T : ParameterResolver<T, R>,
              T : QuotableRegexParameterResolver<T, R> {
    /**
     * Returns a quoted pattern of the parameter resolver
     *
     * @return A quoted pattern of the original [regex parameter resolver][RegexParameterResolver]
     *
     * @see RegexParameterResolver.pattern
     */
    val quotedPattern: Pattern

    override val preferredPattern: Pattern
        get() = quotedPattern
}
