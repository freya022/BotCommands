package com.freya02.botcommands.parameters;

import com.freya02.botcommands.Logging;
import org.slf4j.Logger;

/**
 * Base class for parameter resolvers used in regex commands, slash commands and buttons callbacks
 *
 * @see RegexParameterResolver
 * @see SlashParameterResolver
 * @see ComponentParameterResolver
 */
public abstract class ParameterResolver {
	protected final Logger LOGGER = Logging.getLogger(this);

	private final Class<?> type;

	public ParameterResolver(Class<?> type) {
		this.type = type;
	}

	public Class<?> getType() {
		return type;
	}
}
