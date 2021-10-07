package com.freya02.botcommands.internal.prefixed;

import com.freya02.botcommands.api.entities.Emoji;
import com.freya02.botcommands.api.entities.EmojiOrEmote;
import com.freya02.botcommands.api.parameters.QuotableRegexParameterResolver;
import com.freya02.botcommands.api.prefixed.BaseCommandEvent;
import com.freya02.botcommands.api.prefixed.annotations.Category;
import com.freya02.botcommands.api.prefixed.annotations.*;
import com.freya02.botcommands.internal.Logging;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Utils {
	public static final Logger LOGGER = Logging.getLogger();

	public static String getParameterName(Parameter parameter, String defaultName) {
		if (parameter.isNamePresent()) {
			return parameter.getName();
		} else return defaultName;
	}

	//Description either on class or method
	public static String getDescription(@NotNull TextCommandInfo info) {
		final Description classDescription = info.getCommandMethod().getDeclaringClass().getAnnotation(Description.class);

		if (classDescription != null) {
			return classDescription.value();
		} else {
			return info.getDescription();
		}
	}

	//Category only on class
	public static String getCategory(@NotNull TextCommandInfo info) {
		final Category category = info.getCommandMethod().getDeclaringClass().getAnnotation(Category.class);

		if (category != null) {
			return category.value();
		} else {
			return "No category";
		}
	}

	public static EmbedBuilder generateCommandHelp(TextCommandCandidates candidates, BaseCommandEvent event) {
		final EmbedBuilder builder = event.getDefaultEmbed();

		final TextCommandInfo commandInfo = candidates.last();
		final String name = commandInfo.getPath().getFullPath().replace('/', ' ');

		final String description = Utils.getDescription(commandInfo);
		final String prefix = event.getContext().getPrefix();

		final MessageEmbed.AuthorInfo author = builder.isEmpty() ? null : event.getDefaultEmbed().build().getAuthor();
		if (author != null) {
			builder.setAuthor(author.getName() + " – '" + name + "' command", author.getUrl(), author.getIconUrl());
		} else {
			builder.setAuthor('\'' + name + "' command");
		}
		builder.addField("Description", description, false);

		final ArrayList<TextCommandInfo> reversedCandidates = new ArrayList<>(candidates);
		Collections.reverse(reversedCandidates);

		int i = 1;
		for (TextCommandInfo candidate : reversedCandidates) {
			final List<? extends TextCommandParameter> commandParameters = candidate.getOptionParameters();

			final StringBuilder syntax = new StringBuilder("**Syntax**: " + prefix + name + ' ');
			final StringBuilder example = new StringBuilder("**Example**: " + prefix + name + ' ');

			if (candidate.isRegexCommand()) {
				final boolean needsQuote = hasMultipleQuotable(commandParameters);

				for (TextCommandParameter commandParameter : commandParameters) {
					final Parameter parameter = commandParameter.getParameter();
					final Class<?> boxedType = commandParameter.getBoxedType();

					final String argName = getArgName(needsQuote, parameter, boxedType);
					final String argExample = getArgExample(needsQuote, parameter, boxedType);

					final boolean isOptional = com.freya02.botcommands.internal.utils.Utils.isOptional(parameter);
					syntax.append(isOptional ? '[' : '`').append(argName).append(isOptional ? ']' : '`').append(' ');
					example.append(argExample).append(' ');
				}
			}

			if (candidates.size() == 1) {
				builder.addField("Usage", syntax + "\n" + example, false);
			} else {
				builder.addField("Overload #" + i, syntax + "\n" + example, false);
			}

			i++;
		}

		final List<TextCommandCandidates> textSubcommands = event.getContext().findTextSubcommands(commandInfo.getPath());
		if (textSubcommands != null) {
			final String subcommandHelp = textSubcommands
					.stream()
					.map(TreeSet::first)
					.map(info -> "**" + info.getPath().getNameAt(info.getPath().getNameCount() - commandInfo.getPath().getNameCount()) + "** : " + info.getDescription()) //TODO change name getter
					.collect(Collectors.joining("\n - "));

			if (!subcommandHelp.isBlank()) {
				builder.addField("Subcommands", subcommandHelp, false);
			}
		}

		final Consumer<EmbedBuilder> descConsumer = commandInfo.getInstance().getDetailedDescription();
		if (descConsumer != null) {
			descConsumer.accept(builder);
		}

		return builder;
	}

	private static String getArgExample(boolean needsQuote, Parameter parameter, Class<?> boxedType) {
		final String argExample;

		if (parameter.isAnnotationPresent(ArgExample.class)) {
			final String argExampleStr = parameter.getAnnotation(ArgExample.class).str();
			if (boxedType == String.class) {
				argExample = needsQuote ? "\"" + argExampleStr + "\"" : argExampleStr;
			} else {
				argExample = argExampleStr;
			}
		} else {
			if (boxedType == String.class) {
				argExample = needsQuote ? "\"foo bar\"" : "foo bar";
			} else if (boxedType == Emoji.class) {
				argExample = ":joy:";
			} else if (boxedType == Integer.class) {
				argExample = String.valueOf(ThreadLocalRandom.current().nextLong(50));
			} else if (boxedType == Long.class) {
				if (parameter.isAnnotationPresent(ID.class)) {
					argExample = String.valueOf(ThreadLocalRandom.current().nextLong(100000000000000000L, 999999999999999999L));
				} else {
					argExample = String.valueOf(ThreadLocalRandom.current().nextLong(50));
				}
			} else if (boxedType == Float.class || boxedType == Double.class) {
				argExample = String.valueOf(ThreadLocalRandom.current().nextDouble(50));
			} else if (boxedType == Emote.class) {
				argExample = "<:kekw:673277564034482178>";
			} else if (boxedType == Guild.class) {
				argExample = "331718482485837825";
			} else if (boxedType == Role.class) {
				argExample = "801161492296499261";
			} else if (boxedType == User.class) {
				argExample = "222046562543468545";
			} else if (boxedType == Member.class) {
				argExample = "<@222046562543468545>";
			} else if (boxedType == TextChannel.class) {
				argExample = "331718482485837825";
			} else if (boxedType == EmojiOrEmote.class) {
				argExample = ":flushed:";
			} else {
				argExample = "?";
			}
		}

		return argExample;
	}

	private static String getArgName(boolean needsQuote, Parameter parameter, Class<?> boxedType) {
		final String argName;
		if (parameter.isAnnotationPresent(ArgName.class)) {
			final String argNameStr = parameter.getAnnotation(ArgName.class).str();
			if (boxedType == String.class) {
				argName = needsQuote ? "\"" + argNameStr + "\"" : argNameStr;
			} else {
				argName = argNameStr;
			}
		} else {
			if (boxedType == String.class) {
				argName = needsQuote ? "\"" + getParameterName(parameter, "string") + "\"" : getParameterName(parameter, "string");
			} else if (boxedType == Emoji.class) {
				argName = "unicode emoji/shortcode";
			} else if (boxedType == Integer.class) {
				argName = getParameterName(parameter, "integer");
			} else if (boxedType == Long.class) {
				if (parameter.isAnnotationPresent(ID.class)) {
					argName = "Entity ID";
				} else {
					argName = getParameterName(parameter, "integer");
				}
			} else if (boxedType == Float.class || boxedType == Double.class) {
				argName = getParameterName(parameter, "decimal");
			} else if (boxedType == Emote.class) {
				argName = "emote/emote id";
			} else if (boxedType == Guild.class) {
				argName = "guild id";
			} else if (boxedType == Role.class) {
				argName = "role mention/role id";
			} else if (boxedType == User.class) {
				argName = "user mention/user id";
			} else if (boxedType == Member.class) {
				argName = "member mention/member id";
			} else if (boxedType == TextChannel.class) {
				argName = "text channel mention/text channel id";
			} else if (boxedType == EmojiOrEmote.class) {
				argName = "emoji/emote";
			} else {
				argName = "?";
				LOGGER.warn("Unknown type: {}", boxedType);
			}
		}

		return argName;
	}

	public static boolean hasMultipleQuotable(List<? extends TextCommandParameter> optionParameters) {
		return optionParameters.stream()
				.filter(p -> p.getResolver() instanceof QuotableRegexParameterResolver)
				.count() > 1;
	}
}
