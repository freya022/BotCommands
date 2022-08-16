package com.freya02.botcommands.api.parameters;

import com.freya02.botcommands.api.Logging;
import kotlin.reflect.KClass;
import kotlin.reflect.KType;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * Base class for parameter resolvers used in regex commands, application commands and buttons callbacks
 * <p>
 * Parameters supported by default:
 * <ul>
 *     <li>{@linkplain String}</li>
 *
 *     <li>boolean</li>
 *     <li>long</li>
 *     <li>double</li>
 *
 *     <li>{@linkplain Emoji}</li>
 *
 *     <li>{@linkplain Role}</li>
 *     <li>{@linkplain User}</li>
 *     <li>{@linkplain Member}</li>
 *     <li>{@linkplain GuildChannel all guild channels (in theory)}</li>
 *     <li>{@linkplain Message} (only message context commands)</li>
 * </ul>
 *
 * You can also check loaded parameter resolvers in the logs, on the "trace" level
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

	public ParameterResolver(@NotNull Class<?> clazz) {
		this(ParameterType.ofClass(clazz));
	}

	public ParameterResolver(@NotNull KClass<?> clazz) {
		this(ParameterType.ofKClass(clazz));
	}

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
