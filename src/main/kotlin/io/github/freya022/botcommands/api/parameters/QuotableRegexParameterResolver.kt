package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.commands.prefixed.annotations.JDATextCommand
import java.util.regex.Pattern

/**
 * Parameter resolver for parameters of [@JDATextCommand][JDATextCommand].
 *
 * This resolver is an extension of [RegexParameterResolver], but the regex pattern is quoted to help command parsing.
 *
 * Needs to be implemented alongside a [ParameterResolver] subclass.
 *
 * @param T Type of the implementation
 * @param R Type of the returned resolved objects
 */
interface QuotableRegexParameterResolver<T, R : Any> : RegexParameterResolver<T, R>
        where T : ParameterResolver<T, R>,
              T : QuotableRegexParameterResolver<T, R> {
    /**
     * A quoted pattern of the parameter resolver.
     *
     * @see RegexParameterResolver.pattern
     */
    val quotedPattern: Pattern

    override val preferredPattern: Pattern
        get() = quotedPattern
}
