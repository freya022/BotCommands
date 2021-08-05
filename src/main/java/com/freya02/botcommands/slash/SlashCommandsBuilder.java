package com.freya02.botcommands.slash;

import com.freya02.botcommands.BContextImpl;
import com.freya02.botcommands.Logging;
import com.freya02.botcommands.Utils;
import com.freya02.botcommands.annotation.Optional;
import com.freya02.botcommands.parameters.ParameterResolvers;
import com.freya02.botcommands.slash.annotations.Choice;
import com.freya02.botcommands.slash.annotations.Choices;
import com.freya02.botcommands.slash.annotations.JdaSlashCommand;
import com.freya02.botcommands.slash.annotations.Option;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.internal.utils.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public final class SlashCommandsBuilder {
	private static final Logger LOGGER = Logging.getLogger();
	private final BContextImpl context;
	private final CachedSlashCommands cachedSlashCommands;
	private final List<Long> slashGuildIds;

	public SlashCommandsBuilder(@NotNull BContextImpl context, List<Long> slashGuildIds) {
		this.context = context;
		this.context.setSlashCommandsBuilder(this);
		this.slashGuildIds = slashGuildIds;
		this.cachedSlashCommands = new CachedSlashCommands(context);
	}

	public void processSlashCommand(SlashCommand slashCommand) {
		for (Method method : slashCommand.getClass().getDeclaredMethods()) {
			try {
				if (method.isAnnotationPresent(JdaSlashCommand.class)) {
					if (!method.canAccess(slashCommand))
						throw new IllegalStateException("Slash command " + method + " is not public");

					if (method.getAnnotation(JdaSlashCommand.class).guildOnly()) {
						if (!Utils.hasFirstParameter(method, SlashEvent.class) && !Utils.hasFirstParameter(method, GuildSlashEvent.class))
							throw new IllegalArgumentException("Slash command at " + method + " must have a GuildSlashEvent or SlashEvent as first parameter");

						if (!Utils.hasFirstParameter(method, GuildSlashEvent.class)) {
							//If type is correct but guild specialization isn't used
							LOGGER.warn("Guild-only command {} uses SlashEvent, consider using GuildSlashEvent to remove warnings related to guild stuff's nullability", method);
						}
					} else {
						if (!Utils.hasFirstParameter(method, SlashEvent.class))
							throw new IllegalArgumentException("Slash command at " + method + " must have a SlashEvent as first parameter");
					}

					final SlashCommandInfo info = new SlashCommandInfo(slashCommand, method);

					LOGGER.debug("Adding command path {} for method {}", info.getPath(), method);
					context.addSlashCommand(info.getPath(), info);
				}
			} catch (Exception e) {
				throw new RuntimeException("An exception occurred while processing slash command at " + method, e);
			}
		}
	}

	public void postProcess() throws IOException {
		context.getJDA().setRequiredScopes("applications.commands");

		cachedSlashCommands.computeCommands();
		if (cachedSlashCommands.shouldUpdateGlobalCommands()) {
			cachedSlashCommands.updateGlobalCommands();
			LOGGER.debug("Global commands were updated");
		} else {
			LOGGER.debug("Global commands does not have to be updated");
		}

		final List<Guild> guildCache;
		if (context.getJDA().getShardManager() != null) {
			guildCache = context.getJDA().getShardManager().getGuilds();
		} else {
			guildCache = context.getJDA().getGuilds();
		}

		tryUpdateGuildCommands(guildCache);
	}

	public boolean tryUpdateGuildCommands(Iterable<Guild> guilds) throws IOException {
		boolean changed = false;

		List<ImmutablePair<Guild, CompletableFuture<?>>> commandUpdatePairs = new ArrayList<>();
		for (Guild guild : guilds) {
			if (!slashGuildIds.isEmpty() && !slashGuildIds.contains(guild.getIdLong())) continue;

			cachedSlashCommands.computeGuildCommands(guild);
			if (cachedSlashCommands.shouldUpdateGuildCommands(guild)) {
				changed = true;

				commandUpdatePairs.add(new ImmutablePair<>(guild, cachedSlashCommands.updateGuildCommands(guild)));
				LOGGER.debug("Guild '{}' ({}) commands were updated", guild.getName(), guild.getId());
			} else {
				LOGGER.debug("Guild '{}' ({}) commands does not have to be updated", guild.getName(), guild.getId());
			}
		}

		final List<Long> missedGuilds = new ArrayList<>();
		for (ImmutablePair<Guild, CompletableFuture<?>> commandUpdatePair : commandUpdatePairs) {
			try {
				commandUpdatePair.getRight().join();
			} catch (CompletionException e) { // Check missing access exceptions
				if (e.getCause() instanceof ErrorResponseException) {
					if (((ErrorResponseException) e.getCause()).getErrorResponse() == ErrorResponse.MISSING_ACCESS) {
						final Guild guild = commandUpdatePair.getLeft();

						final String inviteUrl = context.getJDA().getInviteUrl() + "&guild_id=" + guild.getId();

						LOGGER.warn("Could not register guild commands for guild '{}' ({}) as it appears the OAuth2 grants misses applications.commands, you can re-invite the bot in this guild with its already existing permission with this link: {}", guild.getName(), guild.getId(), inviteUrl);
						context.getRegistrationListeners().forEach(r -> r.onGuildSlashCommandMissingAccess(guild, inviteUrl));

						missedGuilds.add(guild.getIdLong());

						continue;
					}
				}

				throw e;
			}
		}

		for (Guild guild : guilds) {
			if (!slashGuildIds.isEmpty() && !slashGuildIds.contains(guild.getIdLong())) continue;
			if (missedGuilds.contains(guild.getIdLong())) continue; //Missing the OAuth2 applications.commands scope in this guild

			cachedSlashCommands.computeGuildPrivileges(guild);
			if (cachedSlashCommands.shouldUpdateGuildPrivileges(guild)) {
				changed = true;

				cachedSlashCommands.updateGuildPrivileges(guild);
				LOGGER.debug("Guild '{}' ({}) commands privileges were updated", guild.getName(), guild.getId());
			} else {
				LOGGER.debug("Guild '{}' ({}) commands privileges does not have to be updated", guild.getName(), guild.getId());
			}
		}

		return changed;
	}

	static void appendCommands(List<Command> commands, StringBuilder sb) {
		for (Command command : commands) {
			final StringJoiner joiner = new StringJoiner("] [", "[", "]").setEmptyValue("");
			for (Command.Option option : command.getOptions()) {
				joiner.add(option.getType().name());
			}

			sb.append(" - ").append(command.getName()).append(" ").append(joiner).append("\n");
		}
	}

	static List<OptionData> getMethodOptions(SlashCommandInfo info) {
		final List<OptionData> list = new ArrayList<>();

		Parameter[] parameters = info.getCommandMethod().getParameters();
		for (int i = 1, parametersLength = parameters.length; i < parametersLength; i++) {
			Parameter parameter = parameters[i];

			final Class<?> type = parameter.getType();
			final Option option = parameter.getAnnotation(Option.class);

			final String name;
			final String description;
			if (option == null) {
				name = getOptionName(parameter);
				description = "No description";
			} else {
				if (option.name().isBlank()) {
					name = getOptionName(parameter);
				} else {
					name = option.name();
				}

				if (option.description().isBlank()) {
					description = "No description";
				} else {
					description = option.description();
				}
			}

			final OptionData data;
			if (type == User.class || type == Member.class) {
				data = new OptionData(OptionType.USER, name, description);
			} else if (type == Role.class) {
				data = new OptionData(OptionType.ROLE, name, description);
			} else if (type == TextChannel.class) {
				data = new OptionData(OptionType.CHANNEL, name, description);
			} else if (type == IMentionable.class) {
				data = new OptionData(OptionType.MENTIONABLE, name, description);
			} else if (type == boolean.class) {
				data = new OptionData(OptionType.BOOLEAN, name, description);
			} else if (type == long.class) {
				data = new OptionData(OptionType.INTEGER, name, description);

				final Choices choices = parameter.getAnnotation(Choices.class);
				if (choices != null) {
					for (Choice choice : choices.value()) {
						data.addChoice(choice.name(), choice.intValue());
					}
				}
			} else if (type == double.class) {
				data = new OptionData(OptionType.NUMBER, name, description);

				final Choices choices = parameter.getAnnotation(Choices.class);
				if (choices != null) {
					for (Choice choice : choices.value()) {
						data.addChoice(choice.name(), choice.doubleValue());
					}
				}
			} else if (ParameterResolvers.exists(type)) {
				data = new OptionData(OptionType.STRING, name, description);

				final Choices choices = parameter.getAnnotation(Choices.class);
				if (choices != null) {
					for (Choice choice : choices.value()) {
						data.addChoice(choice.name(), choice.value());
					}
				}
			} else {
				throw new IllegalArgumentException("Unknown slash command option: " + type.getName());
			}

			list.add(data);

			data.setRequired(!parameter.isAnnotationPresent(Optional.class));
		}

		return list;
	}

	private static String getOptionName(Parameter parameter) {
		if (!parameter.isNamePresent())
			throw new RuntimeException("Parameter name cannot be deduced as the slash command option's name is not specified on: " + parameter);

		final String name = parameter.getName();
		final int nameLength = name.length();
		
		final StringBuilder optionNameBuilder = new StringBuilder(nameLength + 10); //I doubt you'd have a parameter long enough to have more than 10 underscores
		for (int i = 0; i < nameLength; i++) {
			final char c = name.charAt(i);

			if (Character.isUpperCase(c)) {
				optionNameBuilder.append('_').append(Character.toLowerCase(c));
			} else {
				optionNameBuilder.append(c);
			}
		}

		return optionNameBuilder.toString();
	}

	static String getName(String path) {
		final int i = path.indexOf('/');
		if (i == -1) return path;

		return path.substring(i + 1);
	}

	@Nonnull
	static String getParent(String path) {
		return path.substring(0, path.lastIndexOf('/'));
	}
}