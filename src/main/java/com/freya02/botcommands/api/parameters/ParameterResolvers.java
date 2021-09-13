package com.freya02.botcommands.api.parameters;

import com.freya02.botcommands.api.entities.Emoji;
import com.freya02.botcommands.api.entities.EmojiOrEmote;
import com.freya02.botcommands.internal.parameters.*;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
 *     <li>{@linkplain TextChannel}</li>
 *     <li>{@linkplain Message} (only message context commands)</li>
 * </ul>
 */
public class ParameterResolvers {
	private static final Map<Class<?>, ParameterResolver> map = new HashMap<>();
	
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
		register(new MemberResolver());
		register(new MentionableResolver());
		register(new RoleResolver());
		register(new StringResolver());
		register(new TextChannelResolver());
		register(new UserResolver());
		register(new MessageResolver());
	}

	public static void register(@NotNull ParameterResolver resolver) {
		boolean hasInterface = false;
		for (Class<?> possibleInterface : possibleInterfaces) {
			if (possibleInterface.isAssignableFrom(resolver.getClass())) {
				 hasInterface = true;
				 
				 break;
			}
		}

		if (!hasInterface)
			throw new IllegalArgumentException("The resolver should implement at least one of these interfaces: " + possibleInterfaces.stream().map(Class::getSimpleName).collect(Collectors.joining(", ")));

		map.put(resolver.getType(), resolver);
	}

	@Nullable
	public static ParameterResolver of(Class<?> type) {
		return map.get(type);
	}

	public static boolean exists(Class<?> type) {
		return map.containsKey(type);
	}
}
