package com.freya02.botcommands.api.parameters;

import com.freya02.botcommands.internal.Logging;
import org.slf4j.Logger;

/**
 * Base class for parameter resolvers used in regex commands, application commands and buttons callbacks
 *
 * @see RegexParameterResolver
 * @see QuotableRegexParameterResolver
 * @see ComponentParameterResolver
 * @see SlashParameterResolver
 * @see MessageContextParameterResolver
 * @see UserContextParameterResolver
 */
public abstract class ParameterResolver {
	protected final Logger LOGGER = Logging.getLogger(this);

	private final Class<?> type;

	/**
	 * Constructs a new parameter resolver
	 *
	 * @param type Type of the parameter being resolved
	 */
	public ParameterResolver(Class<?> type) {
		this.type = type;
	}

	public Class<?> getType() {
		return type;
	}
}
