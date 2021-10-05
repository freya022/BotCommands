package com.freya02.botcommands.internal.prefixed;

import com.freya02.botcommands.api.entities.Emoji;
import com.freya02.botcommands.api.entities.EmojiOrEmote;
import com.freya02.botcommands.api.prefixed.BaseCommandEvent;
import com.freya02.botcommands.api.prefixed.annotations.ArgExample;
import com.freya02.botcommands.api.prefixed.annotations.ArgName;
import com.freya02.botcommands.api.prefixed.annotations.ID;
import com.freya02.botcommands.internal.Logging;
import com.freya02.botcommands.internal.MethodParameters;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import org.slf4j.Logger;

import java.lang.reflect.Parameter;
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

	public static EmbedBuilder generateHelp(TextCommandInfo commandInfo, BaseCommandEvent event) {
		final EmbedBuilder builder = event.getDefaultEmbed();

		final String name = commandInfo.getPath().getFullPath().replace('/', ' ');

		final String description = commandInfo.getDescription();
		final MethodParameters<TextCommandParameter> methodPatterns = commandInfo.getParameters();
		final String prefix = event.getContext().getPrefix();

		final MessageEmbed.AuthorInfo author = builder.isEmpty() ? null : event.getDefaultEmbed().build().getAuthor();
		if (author != null) {
			builder.setAuthor(author.getName() + " – '" + name + "' command", author.getUrl(), author.getIconUrl());
		} else {
			builder.setAuthor('\'' + name + "' command");
		}
		builder.addField("Description", description, false);

		for (int i = 0; i < methodPatterns.size(); i++) {
			TextCommandParameter methodPattern = methodPatterns.get(i);

			final StringBuilder syntax = new StringBuilder("**Syntax**: ");
			final StringBuilder example = new StringBuilder("**Example**: " + prefix + name + ' ');
			final Parameter[] parameters = commandInfo.getCommandMethod().getParameters();
			boolean hasEmoji = methodPatterns.stream().anyMatch(p -> p.getBoxedType() == Emoji.class);
			for (int j = 1; j < parameters.length; j++) {
				Parameter parameter = methodPattern.getParameter();
				final Class<?> type = methodPattern.getBoxedType();

				final String argName;
				final String argExample;
				if (parameter.isAnnotationPresent(ArgName.class)) {
					final String argNameStr = parameter.getAnnotation(ArgName.class).str();
					if (type == String.class) {
						argName = hasEmoji ? "\"" + argNameStr + "\"" : argNameStr;
					} else {
						argName = argNameStr;
					}
				} else {
					if (type == String.class) {
						argName = hasEmoji ? "\"" + getParameterName(parameter, "string") + "\"" : getParameterName(parameter, "string");
					} else if (type == Emoji.class) {
						argName = "unicode emoji/shortcode";
					} else if (type == int.class) {
						argName = getParameterName(parameter, "integer");
					} else if (type == long.class) {
						if (parameter.isAnnotationPresent(ID.class)) {
							argName = "Entity ID";
						} else {
							argName = getParameterName(parameter, "integer");
						}
					} else if (type == float.class || type == double.class) {
						argName = getParameterName(parameter, "decimal");
					} else if (type == Emote.class) {
						argName = "emote/emote id";
					} else if (type == Guild.class) {
						argName = "guild id";
					} else if (type == Role.class) {
						argName = "role mention/role id";
					} else if (type == User.class) {
						argName = "user mention/user id";
					} else if (type == Member.class) {
						argName = "member mention/member id";
					} else if (type == TextChannel.class) {
						argName = "text channel mention/text channel id";
					} else if (type == EmojiOrEmote.class) {
						argName = "emoji/emote";
					} else {
						argName = "?";
						LOGGER.warn("Unknown type: {}", type);
					}
				}

				if (parameter.isAnnotationPresent(ArgExample.class)) {
					final String argExampleStr = parameter.getAnnotation(ArgExample.class).str();
					if (type == String.class) {
						argExample = hasEmoji ? "\"" + argExampleStr + "\"" : argExampleStr;
					} else {
						argExample = argExampleStr;
					}
				} else {
					if (type == String.class) {
						argExample = hasEmoji ? "\"foo bar\"" : "foo bar";
					} else if (type == Emoji.class) {
						argExample = ":joy:";
					} else if (type == int.class) {
						argExample = String.valueOf(ThreadLocalRandom.current().nextLong(50));
					} else if (type == long.class) {
						if (parameter.isAnnotationPresent(ID.class)) {
							argExample = String.valueOf(ThreadLocalRandom.current().nextLong(100000000000000000L, 999999999999999999L));
						} else {
							argExample = String.valueOf(ThreadLocalRandom.current().nextLong(50));
						}
					} else if (type == float.class || type == double.class) {
						argExample = String.valueOf(ThreadLocalRandom.current().nextDouble(50));
					} else if (type == Emote.class) {
						argExample = "<:kekw:673277564034482178>";
					} else if (type == Guild.class) {
						argExample = "331718482485837825";
					} else if (type == Role.class) {
						argExample = "801161492296499261";
					} else if (type == User.class) {
						argExample = "222046562543468545";
					} else if (type == Member.class) {
						argExample = "<@222046562543468545>";
					} else if (type == TextChannel.class) {
						argExample = "331718482485837825";
					} else if (type == EmojiOrEmote.class) {
						argExample = ":flushed:";
					} else {
						argExample = "?";
					}
				}

				final boolean isOptional = com.freya02.botcommands.internal.utils.Utils.isOptional(parameter);
				syntax.append(isOptional ? '[' : '`').append(argName).append(isOptional ? ']' : '`').append(' ');
				example.append(argExample).append(' ');
			}

			if (methodPatterns.size() == 1) {
				builder.addField("Usage", syntax + "\n" + example, false);
			} else {
				builder.addField("Overload #" + (i + 1), syntax + "\n" + example, false);
			}
		}

		final String subcommandHelp = event.getContext().findTextSubcommands(commandInfo.getPath())
				.stream()
				.map(TreeSet::first)
				.map(info -> "**" + info.getPath().getName() + "** : " + info.getDescription())
				.collect(Collectors.joining("\n - "));

		if (!subcommandHelp.isBlank()) {
			builder.addField("Subcommands", subcommandHelp, false);
		}

		final Consumer<EmbedBuilder> descConsumer = commandInfo.getInstance().getDetailedDescription();
		if (descConsumer != null) {
			descConsumer.accept(builder);
		}

		return builder;
	}
}
