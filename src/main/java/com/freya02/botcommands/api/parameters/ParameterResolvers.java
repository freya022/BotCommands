package com.freya02.botcommands.api.parameters;

import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.entities.Emoji;
import com.freya02.botcommands.api.entities.EmojiOrEmote;
import com.freya02.botcommands.internal.parameters.*;
import com.freya02.botcommands.internal.parameters.channels.*;
import com.freya02.botcommands.internal.utils.ReflectionUtils;
import kotlin.reflect.KType;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Has all the parameter resolvers registered here,
 * they help in resolving method parameters for regex commands, application commands and component callbacks<br><br>
 *
 * Supported parameters:
 * <ul>
 *     <li>{@linkplain String}</li>
 *
 *     <li>boolean</li>
 *     <li>long</li>
 *     <li>double</li>
 *
 *     <li>{@linkplain Emoji}</li>
 *     <li>{@linkplain Emote}</li>
 *     <li>{@linkplain EmojiOrEmote}</li>
 *
 *     <li>{@linkplain Role}</li>
 *     <li>{@linkplain User}</li>
 *     <li>{@linkplain Member}</li>
 *     <li>{@linkplain GuildChannel all guild channels (in theory)}</li>
 *     <li>{@linkplain Message} (only message context commands)</li>
 * </ul>
 */
public class ParameterResolvers {
	private static final Logger LOGGER = Logging.getLogger();
	private static final Map<KType, ParameterResolver> map = new HashMap<>();

	private static final List<Class<?>> possibleInterfaces = List.of(
			RegexParameterResolver.class,
			SlashParameterResolver.class,
			ComponentParameterResolver.class,
			UserContextParameterResolver.class,
			MessageContextParameterResolver.class,
			CustomResolver.class
	);

	static {
		register(new BooleanResolver());
		register(new DoubleResolver());
		register(new EmojiOrEmoteResolver());
		register(new EmojiResolver());
		register(new EmoteResolver());
		register(new GuildResolver());
		register(new LongResolver());
		register(new IntegerResolver());
		register(new MemberResolver());
		register(new MentionableResolver());
		register(new RoleResolver());
		register(new StringResolver());

		register(new GuildChannelResolver());
		register(new TextChannelResolver());
		register(new ThreadChannelResolver());
		register(new VoiceChannelResolver());
		register(new NewsChannelResolver());
		register(new StageChannelResolver());
		register(new CategoryResolver());

		register(new UserResolver());
		register(new MessageResolver());
	}

	public static void register(@NotNull ParameterResolver resolver) {
		final boolean isCompatible = hasCompatibleInterface(resolver);

		if (!isCompatible)
			throw new IllegalArgumentException("The resolver should implement at least one of these interfaces: " + possibleInterfaces.stream().map(Class::getSimpleName).collect(Collectors.joining(", ")));

		final ParameterResolver oldResolver = map.put(resolver.getType(), resolver);

		if (oldResolver != null) {
			LOGGER.warn("An old parameter resolver ({}) got overridden by another ({}) at: {}",
					oldResolver.getClass().getName(),
					resolver.getClass().getName(),
					ReflectionUtils.formatCallerMethod());
		}
	}

	private static boolean hasCompatibleInterface(@NotNull ParameterResolver resolver) {
		for (Class<?> possibleInterface : possibleInterfaces) {
			if (possibleInterface.isAssignableFrom(resolver.getClass())) {
				 return true;
			}
		}

		return false;
	}

	@Nullable
	public static ParameterResolver of(@NotNull ParameterType type) {
		return map.get(type.getType());
	}

	public static boolean exists(@NotNull ParameterType type) {
		return map.containsKey(type.getType());
	}
}
