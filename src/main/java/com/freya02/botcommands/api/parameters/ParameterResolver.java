package com.freya02.botcommands.api.parameters;

import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.core.entities.UserUnion;
import com.freya02.botcommands.api.core.service.annotations.InterfacedService;
import com.freya02.botcommands.api.core.utils.ReflectionUtils;
import kotlin.reflect.KClass;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * Base class for parameter resolvers used in regex commands, application commands and buttons callbacks
 * <p>
 * Parameters supported by default:
 * <ul>
 *     <li>{@link String}</li>
 *
 *     <li>boolean</li>
 *     <li>long</li>
 *     <li>double</li>
 *
 *     <li>{@link Emoji}</li>
 *
 *     <li>{@link Role}</li>
 *     <li>{@link User}</li>
 *     <li>{@link Member}</li>
 *     <li>{@link UserUnion}</li>
 *     <li>{@link GuildChannel all guild channels subtypes (in theory)}</li>
 *     <li>{@link Message} (only message context commands)</li>
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
 * @see ICustomResolver
 */
@SuppressWarnings("unused") //T is used for the inheritance constraint
@InterfacedService(acceptMultiple = true)
public abstract class ParameterResolver<T extends ParameterResolver<T, R>, R> {
	protected final Logger LOGGER = Logging.getLogger(this);

	private final KClass<R> jvmErasure;

	/**
	 * Constructs a new parameter resolver
	 *
	 * @param clazz Class of the parameter being resolved
	 */
	public ParameterResolver(@NotNull Class<R> clazz) {
		this.jvmErasure = ReflectionUtils.toKotlin(clazz);
	}

	/**
	 * Constructs a new parameter resolver
	 *
	 * @param clazz Class of the parameter being resolved
	 */
	public ParameterResolver(@NotNull KClass<R> clazz) {
		this.jvmErasure = clazz;
	}

	public KClass<R> getJvmErasure() {
		return jvmErasure;
	}
}
