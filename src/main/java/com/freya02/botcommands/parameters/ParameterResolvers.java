package com.freya02.botcommands.parameters;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Has all the parameter resolvers registered here, they help in resolving method parameters for regex commands, slash commands and button callbacks
 */
public class ParameterResolvers {
	private static final Map<Class<?>, ParameterResolver> map = new HashMap<>();

	static {
		register(new BooleanResolver());
		register(new DoubleResolver());
		register(new EmojiOrEmoteResolver());
		register(new EmojiResolver());
		register(new EmoteResolver());
		register(new GuildResolver());
		register(new LongResolver());
		register(new MemberResolver());
		register(new MentionableResolver());
		register(new RoleResolver());
		register(new StringResolver());
		register(new TextChannelResolver());
		register(new UserResolver());
	}

	/**
	 * Registers a parameter resolver, must have one or more of the 3 interfaces
	 *
	 * @param resolver Your own ParameterResolver to register
	 */
	public static void register(@NotNull ParameterResolver resolver) {
		if (!(resolver instanceof RegexParameterResolver) && !(resolver instanceof SlashParameterResolver) && !(resolver instanceof ButtonParameterResolver))
			throw new IllegalArgumentException("The resolver should work at least for a regex parameter, a slash parameter or a button parameter");

		map.put(resolver.getType(), resolver);
	}

	@Nullable
	public static ParameterResolver of(Class<?> type) {
		return map.get(type);
	}
}
