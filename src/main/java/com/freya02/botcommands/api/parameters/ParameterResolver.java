package com.freya02.botcommands.api.parameters;

import com.freya02.botcommands.api.Logging;
import kotlin.reflect.KType;
import org.jetbrains.annotations.NotNull;
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

	private final KType type;

	/**
	 * Constructs a new parameter resolver
	 *
	 * @param type Type of the parameter being resolved
	 */
	public ParameterResolver(@NotNull ParameterType type) {
		this.type = type.getType();
	}

	public KType getType() {
		return type;
	}
}
