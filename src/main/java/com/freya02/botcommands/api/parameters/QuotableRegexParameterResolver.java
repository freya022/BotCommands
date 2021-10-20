package com.freya02.botcommands.api.parameters;

import java.util.regex.Pattern;

/**
 * Interface which indicates this class can resolve parameters for regex commands
 * <br><b>Must be used with {@link RegexParameterResolver}</b>
 */
public interface QuotableRegexParameterResolver {
	Pattern getQuotedPattern();
}
