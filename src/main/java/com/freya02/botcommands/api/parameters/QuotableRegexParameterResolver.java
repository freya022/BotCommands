package com.freya02.botcommands.api.parameters;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * Interface which indicates this class can resolve parameters for regex commands.
 * <br><b>Must be used with {@link RegexParameterResolver}.</b>
 */
public interface QuotableRegexParameterResolver<T extends ParameterResolver<T, R> & QuotableRegexParameterResolver<T, R>, R> extends RegexParameterResolver<T, R> {
	/**
	 * Returns a quoted pattern of the parameter resolver
	 *
	 * @return A quoted pattern of the original {@link RegexParameterResolver regex parameter resolver}
	 *
	 * @see RegexParameterResolver#getPattern()
	 */
	@NotNull
	Pattern getQuotedPattern();

	@NotNull
	@Override
	default Pattern getPreferredPattern() {
		return getQuotedPattern();
	}
}
