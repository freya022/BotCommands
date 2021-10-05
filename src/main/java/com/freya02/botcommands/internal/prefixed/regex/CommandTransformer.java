package com.freya02.botcommands.internal.prefixed.regex;

import com.freya02.botcommands.api.entities.Emoji;
import com.freya02.botcommands.api.entities.EmojiOrEmote;
import com.freya02.botcommands.api.prefixed.BaseCommandEvent;
import com.freya02.botcommands.api.prefixed.TextCommand;
import com.freya02.botcommands.api.prefixed.annotations.MethodOrder;
import com.freya02.botcommands.internal.Logging;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.entities.*;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommandTransformer {
	private static final Logger LOGGER = Logging.getLogger();

	public static final Map<Class<?>, ArgumentFunction> map = new HashMap<>() {{
		put(String.class, new ArgumentFunction("\"(\\X+?)\"", 1, String.class));
		put(Emoji.class, new ArgumentFunction("(\"[^\"]+\")", 1, Emoji.class));
		put(EmojiOrEmote.class, new ArgumentFunction("(\"[^\"]+\")", 1, EmojiOrEmote.class));

		put(Boolean.class, new ArgumentFunction("[a-zA-Z]{4,5}", 1, Boolean.class));
		put(Long.class, new ArgumentFunction("(\\d+)", 1, Long.class));
		put(Double.class, new ArgumentFunction("(-?[0-9]*[.]?[0-9]+)", 1, Double.class));

		put(Emote.class, new ArgumentFunction("(?><a?:([a-zA-Z0-9_]+):)?([0-9]+)>?", 2, Emote.class));
		put(Guild.class, new ArgumentFunction("(\\d+)", 1, Guild.class));
		put(Role.class, new ArgumentFunction("(?><@&)?(\\d+)>?", 1, Role.class));
		put(User.class, new ArgumentFunction("(?><@!?)?(\\d+)>?", 1, User.class));
		put(Member.class, new ArgumentFunction("(?><@!?)?(\\d+)>?", 1, Member.class));
		put(TextChannel.class, new ArgumentFunction("(?><#)?(\\d+)>?", 1, TextChannel.class));
	}};

	public static List<MethodPattern> getMethodPatterns(TextCommand command) {
		List<MethodPattern> list = new ArrayList<>();

		final List<Method> candidates = new ArrayList<>();
		final Method[] methods = command.getClass().getDeclaredMethods();
		for (Method method : methods) {
			if (!method.isAnnotationPresent(MethodOrder.class)) continue;
			if (Modifier.isStatic(method.getModifiers())) {
				LOGGER.warn("Regex commands cannot be static, at {}", method.toGenericString());

				continue;
			} else if (!method.canAccess(command)) {
				LOGGER.warn("Regex commands must be public, at {}", method.toGenericString());

				continue;
			}

			final List<Class<?>> parameterTypes = new ArrayList<>(Arrays.asList(method.getParameterTypes()));

			if (!Utils.hasFirstParameter(method, BaseCommandEvent.class)) {
				LOGGER.error("Error: method {} must at least have a BaseCommandEvent argument on first parameter", method);

				continue;
			}

			parameterTypes.remove(0);
			if (parameterTypes.stream().anyMatch(key -> !map.containsKey(Utils.getBoxedType(key)))) {
				LOGGER.error("Unsupported parameter types in {}", method);
				LOGGER.error("Unsupported types : {}", parameterTypes.stream().filter(c -> !map.containsKey(Utils.getBoxedType(c))).map(Class::getSimpleName).collect(Collectors.joining(", ")));

				continue;
			}

			candidates.add(method);
		}

		final boolean hasSpecificOrder = candidates.stream().anyMatch(m -> m.getAnnotation(MethodOrder.class).value() != 0);
		candidates.sort(new MethodComparator());
		for (Method method : candidates) {
			if (hasSpecificOrder) {
				if (method.getAnnotation(MethodOrder.class).value() == 0) {
					LOGGER.error("Method {} does not have an order specified but this class has at least one specified. Do not forget order cannot be 0", method);
				}
			}

			final List<Class<?>> parameterTypes = new ArrayList<>(Arrays.asList(method.getParameterTypes()));
			parameterTypes.remove(0);

			boolean needsQuotes = parameterTypes.stream().filter(c -> c == Emoji.class || c == EmojiOrEmote.class || c == String.class).count() > 1;
			List<ArgumentFunction> groupsList = new ArrayList<>();
			StringBuilder patternBuilder = new StringBuilder(128);
			for (int i = 0, parameterTypesSize = parameterTypes.size(); i < parameterTypesSize; i++) {
				Class<?> parameterType = Utils.getBoxedType(parameterTypes.get(i));

				ArgumentFunction argumentFunction = map.get(parameterType);
				if (i + 1 != parameterTypesSize) { //Replace greedy quantifier by a lazy one in situations where parsing shouldn't change, to save steps
					if (parameterType == String.class) {
						argumentFunction = argumentFunction.optimize(needsQuotes ? "\"(\\X+?)\"" : "(\\X+?)");
					} else if (parameterType == Emoji.class || parameterType == EmojiOrEmote.class) {
						argumentFunction = argumentFunction.optimize(needsQuotes ? "\"([^\"]+?)\"" : "([^\"]+?)");
					}
				} else if (!needsQuotes) {
					if (parameterType == String.class) {
						argumentFunction = argumentFunction.optimize("(\\X+)");
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

			LOGGER.debug("Method: {}, pattern: '{}'", Utils.formatMethodShort(method), regex);
		}

		return list;
	}

	public static boolean isOptional(Method method, int paramNumber) {
		return Utils.isOptional(method.getParameters()[paramNumber + 1 /* +1 for BaseCommandEvent */]);
	}
}
