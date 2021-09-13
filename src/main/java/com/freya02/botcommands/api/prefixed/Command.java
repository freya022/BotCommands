package com.freya02.botcommands.api.prefixed;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.CommandsBuilder;
import com.freya02.botcommands.api.entities.Emoji;
import com.freya02.botcommands.api.entities.EmojiOrEmote;
import com.freya02.botcommands.api.prefixed.annotations.ArgExample;
import com.freya02.botcommands.api.prefixed.annotations.ArgName;
import com.freya02.botcommands.api.prefixed.annotations.ID;
import com.freya02.botcommands.api.prefixed.annotations.JdaCommand;
import com.freya02.botcommands.internal.Logging;
import com.freya02.botcommands.internal.prefixed.regex.MethodPattern;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.lang.reflect.Parameter;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * <p>Extend this class on the class used for a <b>command or a subcommand</b></p>
 * <p>You also need to use the {@linkplain JdaCommand @JdaCommand} annotation in order to register a command with {@linkplain CommandsBuilder}</p>
 */
public abstract class Command {
	private static final Logger LOGGER = Logging.getLogger();
	private EmbedBuilder helpBuilder = null;

	private final CommandInfo info;

	public Command(BContext context) {
		Objects.requireNonNull(context, "The passed BContext to this Command (at " + this.getClass().getName() + ") cannot not be null");

		info = new CommandInfo(this, context);
	}

	/** Called when the command is invoked by an user
	 *
	 * @param event {@linkplain CommandEvent} object for gathering arguments / author / channel / etc...
	 */
	public void execute(CommandEvent event) {
		showHelp(event);
	}

	/**
	 * <p>Returns a detailed embed of what the command is, it is used by the internal 'help' command</p>
	 * <p>The 'help' command will automatically set the embed title to be "<code>Command '[command_name]'</code>" but can be overridden</p>
	 * <p>It will also set the embed's description to be the command's description, <b>you can override with {@linkplain EmbedBuilder#setDescription(CharSequence)}</b></p>
	 * @return The EmbedBuilder to use as a detailed description
	 */
	@Nullable
	protected Consumer<EmbedBuilder> getDetailedDescription() { return null; }

	private String getParameterName(Parameter parameter, String defaultName) {
		if (parameter.isNamePresent()) {
			return parameter.getName();
		} else return defaultName;
	}

	private EmbedBuilder generateHelp(BaseCommandEvent event) {
		final EmbedBuilder builder = event.getDefaultEmbed();

		final CommandInfo commandInfo = getInfo();
		final var addSubcommandHelp = commandInfo.isAddSubcommandHelp();
		final var addExecutableHelp = commandInfo.isAddExecutableHelp();

		Command cmd = this;
		StringBuilder nameBuilder = new StringBuilder(commandInfo.getName());
		while ((cmd = cmd.getInfo().getParentCommand()) != null) {
			nameBuilder.insert(0, " ").insert(0, cmd.getInfo().getName());
		}
		final String name = nameBuilder.toString();

		final var description = commandInfo.getDescription();
		final var methodPatterns = commandInfo.getMethodPatterns();
		final var prefix = event.getContext().getPrefix();

		final MessageEmbed.AuthorInfo author = builder.isEmpty() ? null : event.getDefaultEmbed().build().getAuthor();
		if (author != null) {
			builder.setAuthor(author.getName() + " – '" + name + "' command", author.getUrl(), author.getIconUrl());
		} else {
			builder.setAuthor('\'' + name + "' command");
		}
		builder.addField("Description", description, false);

		if (addExecutableHelp) {
			for (int i = 0; i < methodPatterns.size(); i++) {
				MethodPattern methodPattern = methodPatterns.get(i);

				final StringBuilder syntax = new StringBuilder("**Syntax**: ");
				final StringBuilder example = new StringBuilder("**Example**: " + prefix + name + ' ');
				final Parameter[] parameters = methodPattern.method.getParameters();
				boolean hasEmoji = Arrays.stream(parameters).anyMatch(p -> p.getType() == Emoji.class);
				for (int j = 1; j < parameters.length; j++) {
					Parameter parameter = parameters[j];
					final Class<?> type = parameter.getType();

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

					final boolean isOptional = Utils.isOptional(parameter);
					syntax.append(isOptional ? '[' : '`').append(argName).append(isOptional ? ']' : '`').append(' ');
					example.append(argExample).append(' ');
				}

				if (methodPatterns.size() == 1) {
					builder.addField("Usage", syntax + "\n" + example, false);
				} else {
					builder.addField("Overload #" + (i + 1), syntax + "\n" + example, false);
				}
			}
		}

		if (addSubcommandHelp) {
			final List<Command> subcommands = info.getSubcommands();
			if (!subcommands.isEmpty()) {
				final String subcommandHelp = subcommands.stream()
						.map(Command::getInfo)
						.filter(info2 -> !info2.isHidden())
						.map(info2 -> "**" + info2.getName() + "** : " + info2.getDescription())
						.collect(Collectors.joining("\n"));
				builder.addField("Subcommands", subcommandHelp, false);
			}
		}

		final Consumer<EmbedBuilder> descConsumer = getDetailedDescription();
		if (descConsumer != null) {
			descConsumer.accept(builder);
		}

		return builder;
	}

	public synchronized void showHelp(BaseCommandEvent event) {
		final Consumer<BaseCommandEvent> helpConsumer = event.getContext().getHelpConsumer();
		if (helpConsumer != null) {
			helpConsumer.accept(event);

			return;
		}

		if (helpBuilder == null) {
			helpBuilder = generateHelp(event);

			final Consumer<EmbedBuilder> helpBuilderConsumer = event.getContext().getHelpBuilderConsumer();
			if (helpBuilderConsumer != null) helpBuilderConsumer.accept(helpBuilder);
		}

		helpBuilder.setTimestamp(Instant.now());
		helpBuilder.setFooter("Arguments in black boxes `foo` are obligatory, but arguments in brackets [bar] are optional");

		final Member member = event.getMember();
		helpBuilder.setColor(member.getColorRaw());

		event.respond(helpBuilder.build()).queue(null, event.failureReporter("Unable to send help message"));
	}

	public CommandInfo getInfo() {
		return info;
	}
}
