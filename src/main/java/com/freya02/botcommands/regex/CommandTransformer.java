package com.freya02.botcommands.regex;

import com.freya02.botcommands.BaseCommandEvent;
import com.freya02.botcommands.Command;
import com.freya02.botcommands.Emoji;
import com.freya02.botcommands.EmojiImpl;
import com.freya02.botcommands.annotation.Executable;
import com.freya02.botcommands.annotation.Optional;
import com.freya02.botcommands.utils.EmojiResolver;
import net.dv8tion.jda.api.entities.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommandTransformer {
	public static final Map<Class<?>, ArgumentFunction> map = new HashMap<>() {{
		put(String.class, new ArgumentFunction("\"(\\X+?)\"", 1, (e, p) -> p[0]));
		put(Emoji.class, new ArgumentFunction("([^\"]+)", 1, (e, p) -> new EmojiImpl(EmojiResolver.resolveEmojis(p[0]))));

		put(int.class, new ArgumentFunction("(\\d+)", 1, (e, p) -> Integer.valueOf(p[0])));
		put(long.class, new ArgumentFunction("(\\d+)", 1, (e, p) -> Long.valueOf(p[0])));
		put(float.class, new ArgumentFunction("(-?[0-9]*[.]?[0-9]+)", 1, (e, p) -> Float.valueOf(p[0])));
		put(double.class, new ArgumentFunction("(-?[0-9]*[.]?[0-9]+)", 1, (e, p) -> Double.valueOf(p[0])));

		put(Emote.class, new ArgumentFunction("(?><a?:([a-zA-Z0-9_]+):)?([0-9]+)>?", 2, (e, p) -> checkNull(e.getJDA().getEmoteById(p[1]))));
		put(Guild.class, new ArgumentFunction("(\\d+)", 1, (e, p) -> checkNull(e.getJDA().getGuildById(p[0]))));
		put(Role.class, new ArgumentFunction("(?><@&)?(\\d+)>?", 1, (e, p) -> {
			final Role role = checkNull(e.getGuild().getRoleById(p[0]));
			if (role.isPublicRole()) throw new NoSuchElementException();
			return role;
		}));
		put(User.class, new ArgumentFunction("(?><@!?)?(\\d+)>?", 1, (e, p) -> checkNull(e.getJDA().retrieveUserById(p[0]).complete())));
		put(Member.class, new ArgumentFunction("(?><@!?)?(\\d+)>?", 1, (e, p) -> checkNull(e.getGuild().retrieveMemberById(p[0]).complete())));
		put(TextChannel.class, new ArgumentFunction("(?><#)?(\\d+)>?", 1, (e, p) -> checkNull(e.getGuild().getTextChannelById(p[0]))));
	}};

	private static <T> T checkNull(T t) {
		if (t == null) throw new NoSuchElementException();
		return t;
	}

	public static List<MethodPattern> getMethodPatterns(Command command, boolean debug) {
		List<MethodPattern> list = new ArrayList<>();

		final List<Method> candidates = new ArrayList<>();
		final Method[] methods = command.getClass().getMethods();
		for (Method method : methods) {
			if (!method.isAnnotationPresent(Executable.class)) continue;
			final List<Class<?>> parameterTypes = new ArrayList<>(Arrays.asList(method.getParameterTypes()));

			if (parameterTypes.isEmpty() || parameterTypes.get(0) != BaseCommandEvent.class) {
				System.err.println("Error: method " + method + " must at least have a BaseCommandEvent argument on first parameter");

				continue;
			}

			parameterTypes.remove(0);
			if (parameterTypes.stream().anyMatch(key -> !map.containsKey(key))) {
				System.err.println("Error: unsupported parameter types in " + method);
				System.err.println("Unsupported types : " + parameterTypes.stream().filter(c -> !map.containsKey(c)).map(Class::getSimpleName).collect(Collectors.joining(", ")));

				continue;
			}

			candidates.add(method);
		}

		final boolean hasSpecificOrder = candidates.stream().anyMatch(m -> m.getAnnotation(Executable.class).order() != 0);
		candidates.sort(new MethodComparator());
		for (Method method : candidates) {
			if (hasSpecificOrder) {
				if (method.getAnnotation(Executable.class).order() == 0) {
					System.err.println("Method " + method + " does not have an order specified but this class has at least one specified. Do not forget order cannot be 0");
				}
			}

			final List<Class<?>> parameterTypes = new ArrayList<>(Arrays.asList(method.getParameterTypes()));
			parameterTypes.remove(0);

			boolean hasEmoji = parameterTypes.stream().anyMatch(c -> c == Emoji.class);
			List<ArgumentFunction> groupsList = new ArrayList<>();
			StringBuilder patternBuilder = new StringBuilder(128);
			for (int i = 0, parameterTypesSize = parameterTypes.size(); i < parameterTypesSize; i++) {
				Class<?> parameterType = parameterTypes.get(i);

				ArgumentFunction argumentFunction = map.get(parameterType);
				if (i + 1 != parameterTypesSize) { //Replace greedy quantifier by a lazy one in situations where parsing shouldn't change, to save steps
					if (parameterType == String.class) {
						argumentFunction = argumentFunction.optimize(hasEmoji ? "\"(\\X+?)\"" : "(\\X+?)");
					} else if (parameterType == Emoji.class) {
						argumentFunction = argumentFunction.optimize("([^\"]+?)");
					}
				} else if (!hasEmoji) {
					if (parameterType == String.class) {
						argumentFunction = argumentFunction.optimize("(\\X+?)");
					}
				}

				groupsList.add(argumentFunction);
				final boolean isOptional = isOptional(method, i);
				if (isOptional) patternBuilder.append("(?>"); //Add optional support in non-capturing group part 1
				patternBuilder.append(argumentFunction.pattern);

				//If this is the last parameter or the next parameter is optional then use a 0-or-more spaces
				if (i + 1 == parameterTypesSize || (i + 1 < parameterTypesSize && isOptional(method, i + 1))) {
					patternBuilder.append(" *");
				} else {
					patternBuilder.append(" +");
				}
				if (isOptional) patternBuilder.append(")?"); //Add optional support in non-capturing group part 2
			}

			String regex = patternBuilder.toString();
			if (regex.endsWith(" +")) {
				regex = regex.substring(0, regex.length() - 2);
			}
			list.add(new MethodPattern(method, Pattern.compile(regex), groupsList.toArray(new ArgumentFunction[0])));

			if (debug) {
				System.out.println("Method: " + method + ", pattern: '" + regex + "'");
			}
		}

		return list;
	}

	public static boolean isOptional(Method method, int paramNumber) {
		return Arrays.stream(method.getParameterAnnotations()[paramNumber + 1 /* +1 for BaseCommandEvent */]).anyMatch(a -> a.annotationType() == Optional.class);
	}
}
