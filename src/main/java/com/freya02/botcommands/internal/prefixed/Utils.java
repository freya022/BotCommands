package com.freya02.botcommands.internal.prefixed;

import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.entities.Emoji;
import com.freya02.botcommands.api.entities.EmojiOrEmote;
import com.freya02.botcommands.api.parameters.QuotableRegexParameterResolver;
import com.freya02.botcommands.api.prefixed.BaseCommandEvent;
import kotlin.reflect.KParameter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class Utils {
	private static final Logger LOGGER = Logging.getLogger();

	public static String getParameterName(KParameter parameter, String defaultName) {
		if (parameter.getName() != null) {
			return parameter.getName();
		} else return defaultName;
	}

	//Description either on class or method
	@Nullable
	public static String getDescription(@NotNull TextCommandInfo info) {
		throw new UnsupportedOperationException();

//		final Description classDescription = info.getMethod().getDeclaringClass().getAnnotation(Description.class);
//
//		if (classDescription != null) {
//			return classDescription.value();
//		} else if (!info.getDescription().isBlank()) {
//			return info.getDescription();
//		} else {
//			return null;
//		}
	}

	@NotNull
	public static String getNonBlankDescription(@NotNull TextCommandInfo info) {
		return Objects.requireNonNullElse(getDescription(info), "No description");
	}

	//Category only on class
	public static String getCategory(@NotNull TextCommandInfo info) {
		throw new UnsupportedOperationException();

//		final Category category = info.getMethod().getDeclaringClass().getAnnotation(Category.class);
//
//		if (category != null) {
//			return category.value();
//		} else {
//			return "No category";
//		}
	}

	public static EmbedBuilder generateCommandHelp(TextCommandCandidates candidates, BaseCommandEvent event) {
		throw new UnsupportedOperationException();

//		final EmbedBuilder builder = event.getDefaultEmbed();
//
//		final TextCommandInfo commandInfo = candidates.last();
//		final String name = commandInfo.getPath().getFullPath().replace('/', ' ');
//
//		final String description = Utils.getDescription(commandInfo);
//		final String prefix = event.getContext().getPrefix();
//
//		final MessageEmbed.AuthorInfo author = builder.isEmpty() ? null : event.getDefaultEmbed().build().getAuthor();
//		if (author != null) {
//			builder.setAuthor(author.getName() + " – '" + name + "' command", author.getUrl(), author.getIconUrl());
//		} else {
//			builder.setAuthor('\'' + name + "' command");
//		}
//
//		if (description != null) {
//			builder.addField("Description", description, false);
//		}
//
//		final ArrayList<TextCommandInfo> reversedCandidates = new ArrayList<>(candidates);
//		Collections.reverse(reversedCandidates);
//
//		int i = 1;
//		for (TextCommandInfo candidate : reversedCandidates) {
//			final List<? extends TextCommandParameter> commandParameters = candidate.getOptionParameters();
//
//			final StringBuilder syntax = new StringBuilder("**Syntax**: " + prefix + name + ' ');
//			final StringBuilder example = new StringBuilder("**Example**: " + prefix + name + ' ');
//
//			if (candidate.isRegexCommand()) {
//				final boolean needsQuote = hasMultipleQuotable(commandParameters);
//
//				for (TextCommandParameter commandParameter : commandParameters) {
//					final Class<?> boxedType = commandParameter.getBoxedType();
//
//					final String argName = getArgName(needsQuote, commandParameter, boxedType);
//					final String argExample = getArgExample(needsQuote, commandParameter, boxedType);
//
//					final boolean isOptional = commandParameter.isOptional();
//					syntax.append(isOptional ? '[' : '`').append(argName).append(isOptional ? ']' : '`').append(' ');
//					example.append(argExample).append(' ');
//				}
//			}
//
//			final String effectiveCandidateDescription = !candidate.hasDescription()
//					? ""
//					: ("**Description**: " + candidate.getDescription() + "\n");
//			if (candidates.size() == 1) {
//				builder.addField("Usage", effectiveCandidateDescription + syntax + "\n" + example, false);
//			} else {
//				builder.addField("Overload #" + i, effectiveCandidateDescription + syntax + "\n" + example, false);
//			}
//
//			i++;
//		}
//
//		final List<TextCommandCandidates> textSubcommands = event.getContext().findTextSubcommands(commandInfo.getPath());
//		if (textSubcommands != null) {
//			final String subcommandHelp = textSubcommands
//					.stream()
//					.map(TreeSet::first)
//					.map(info -> "**" + info.getPath().getNameAt(info.getPath().getNameCount() - commandInfo.getPath().getNameCount()) + "** : " + Utils.getNonBlankDescription(info))
//					.collect(Collectors.joining("\n - "));
//
//			if (!subcommandHelp.isBlank()) {
//				builder.addField("Subcommands", subcommandHelp, false);
//			}
//		}
//
//		final Consumer<EmbedBuilder> descConsumer = commandInfo.getInstance().getDetailedDescription();
//		if (descConsumer != null) {
//			descConsumer.accept(builder);
//		}
//
//		return builder;
	}

	private static String getArgExample(boolean needsQuote, TextCommandParameter parameter, Class<?> boxedType) {
		final Optional<String> optionalExample = parameter.getData().getOptionalExample();
		if (optionalExample.isPresent()) {
			final String argExampleStr = optionalExample.get();
			if (boxedType == String.class) {
				return needsQuote ? "\"" + argExampleStr + "\"" : argExampleStr;
			} else {
				return argExampleStr;
			}
		} else {
			if (boxedType == String.class) {
				return needsQuote ? "\"foo bar\"" : "foo bar";
			} else if (boxedType == Emoji.class) {
				return ":joy:";
			} else if (boxedType == Integer.class) {
				return String.valueOf(ThreadLocalRandom.current().nextLong(50));
			} else if (boxedType == Long.class) {
				if (parameter.isId()) {
					return String.valueOf(ThreadLocalRandom.current().nextLong(100000000000000000L, 999999999999999999L));
				} else {
					return String.valueOf(ThreadLocalRandom.current().nextLong(50));
				}
			} else if (boxedType == Float.class || boxedType == Double.class) {
				return String.valueOf(ThreadLocalRandom.current().nextDouble(50));
			} else if (boxedType == Emote.class) {
				return "<:kekw:673277564034482178>";
			} else if (boxedType == Guild.class) {
				return "331718482485837825";
			} else if (boxedType == Role.class) {
				return "801161492296499261";
			} else if (boxedType == User.class) {
				return "222046562543468545";
			} else if (boxedType == Member.class) {
				return "<@222046562543468545>";
			} else if (boxedType == TextChannel.class) {
				return "331718482485837825";
			} else if (boxedType == EmojiOrEmote.class) {
				return ":flushed:";
			} else {
				return "?";
			}
		}
	}

	private static String getArgName(boolean needsQuote, TextCommandParameter commandParameter, Class<?> boxedType) {
		final Optional<String> optionalName = commandParameter.getData().getOptionalName();
		if (optionalName.isPresent()) {
			final String argNameStr = optionalName.get();
			if (boxedType == String.class) {
				return needsQuote ? "\"" + argNameStr + "\"" : argNameStr;
			} else {
				return argNameStr;
			}
		} else {
			if (boxedType == String.class) {
				return needsQuote ? "\"" + getParameterName(commandParameter.getKParameter(), "string") + "\"" : getParameterName(commandParameter.getKParameter(), "string");
			} else if (boxedType == Emoji.class) {
				return "unicode emoji/shortcode";
			} else if (boxedType == Integer.class) {
				return getParameterName(commandParameter.getKParameter(), "integer");
			} else if (boxedType == Long.class) {
				if (commandParameter.isId()) {
					return "Entity ID";
				} else {
					return getParameterName(commandParameter.getKParameter(), "integer");
				}
			} else if (boxedType == Float.class || boxedType == Double.class) {
				return getParameterName(commandParameter.getKParameter(), "decimal");
			} else if (boxedType == Emote.class) {
				return "emote/emote id";
			} else if (boxedType == Guild.class) {
				return "guild id";
			} else if (boxedType == Role.class) {
				return "role mention/role id";
			} else if (boxedType == User.class) {
				return "user mention/user id";
			} else if (boxedType == Member.class) {
				return "member mention/member id";
			} else if (boxedType == TextChannel.class) {
				return "text channel mention/text channel id";
			} else if (boxedType == EmojiOrEmote.class) {
				return "emoji/emote";
			} else {
				LOGGER.warn("Unknown type: {}", boxedType);
				return "?";
			}
		}
	}

	public static boolean hasMultipleQuotable(List<? extends TextCommandParameter> optionParameters) {
		return optionParameters.stream()
				.filter(p -> p.getResolver() instanceof QuotableRegexParameterResolver)
				.count() > 1;
	}

	@NotNull
	public static <T extends IMentionable> T findEntity(long id, @NotNull Collection<T> collection, Supplier<@NotNull T> valueSupplier) {
		for (T user : collection) {
			if (user.getIdLong() == id) {
				return user;
			}
		}

		return valueSupplier.get();
	}
}
