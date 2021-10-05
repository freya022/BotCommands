package com.freya02.botcommands.api.parameters;

import java.util.regex.Pattern;

/**
 * Interface which indicates this class can resolve parameters for regex commands
 */
public interface QuotableRegexParameterResolver {
	Pattern getQuotedPattern();
}
