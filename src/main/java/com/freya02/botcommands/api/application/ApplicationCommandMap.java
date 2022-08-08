package com.freya02.botcommands.api.application;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.CommandStatus;
import com.freya02.botcommands.api.SettingsProvider;
import com.freya02.botcommands.internal.application.ApplicationCommandInfo;
import com.freya02.botcommands.internal.application.CommandIdProcessor;
import com.freya02.botcommands.internal.application.CommandMap;
import com.freya02.botcommands.internal.application.MutableCommandMap;
import com.freya02.botcommands.internal.application.context.message.MessageCommandInfo;
import com.freya02.botcommands.internal.application.context.user.UserCommandInfo;
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.utils.AnnotationUtils;
import gnu.trove.set.TLongSet;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ApplicationCommandMap {
	private final Map<Command.Type, CommandMap<ApplicationCommandInfo>> typeMap = Collections.synchronizedMap(new EnumMap<>(Command.Type.class));

	@UnmodifiableView
	public Collection<? extends ApplicationCommandInfo> getAllApplicationCommands() {
		return typeMap.values()
				.stream()
				.flatMap(map -> map.values().stream())
				.toList();
	}

	public Stream<? extends ApplicationCommandInfo> getAllApplicationCommandsStream() {
		return typeMap.values()
				.stream()
				.flatMap(map -> map.values().stream());
	}

	public ApplicationCommandInfo get(Command.Type type, CommandPath path) {
		return getTypeMap(type).get(path);
	}

	@UnmodifiableView
	public CommandMap<SlashCommandInfo> getSlashCommands() {
		return getTypeMap(Command.Type.SLASH);
	}

	@UnmodifiableView
	public CommandMap<UserCommandInfo> getUserCommands() {
		return getTypeMap(Command.Type.USER);
	}

	@UnmodifiableView
	public CommandMap<MessageCommandInfo> getMessageCommands() {
		return getTypeMap(Command.Type.MESSAGE);
	}

	@Nullable
	public SlashCommandInfo findSlashCommand(@NotNull CommandPath path) {
		return this.getSlashCommands().get(path);
	}

	@Nullable
	public UserCommandInfo findUserCommand(@NotNull String name) {
		return this.getUserCommands().get(CommandPath.ofName(name));
	}

	@Nullable
	public MessageCommandInfo findMessageCommand(@NotNull String name) {
		return this.getMessageCommands().get(CommandPath.ofName(name));
	}

	@SuppressWarnings("unchecked")
	public <T extends ApplicationCommandInfo> CommandMap<T> getTypeMap(Command.Type type) {
		return (CommandMap<T>) typeMap.computeIfAbsent(type, x -> new MutableCommandMap<>());
	}

	public List<ApplicationCommandInfo> filterByGuild(@NotNull BContext context, @Nullable Guild guild, @Nullable CommandIdProcessor commandIdProcessor) {
		return getAllApplicationCommandsStream()
				.filter(info -> {
					if (info.getScope() == CommandScope.GUILD && guild == null) { //Do not update guild-only commands in global context
						return false;
					} else if (info.getScope() != CommandScope.GUILD && guild != null) { //Do not update global commands in guild context
						return false;
					}

					//Get the actual usable commands in this context (dm or guild)
					if (guild == null) return true;

					if (info.isTestOnly()) { //Do not include commands in guilds not present in the test guild IDs
						final TLongSet effectiveTestGuildIds = AnnotationUtils.getEffectiveTestGuildIds(context, info.getMethod());

						if (!effectiveTestGuildIds.contains(guild.getIdLong())) {
							return false;
						}
					}

					if (commandIdProcessor == null)
						throw new IllegalArgumentException("Command ID processor should not be null if guild isn't null");

					final String commandId = info.getCommandId();
					if (commandId != null) {
						final CommandStatus commandStatus = commandIdProcessor.getStatus(info.getPath(), commandId, guild.getIdLong());

						if (commandStatus == CommandStatus.DISABLED) {
							return false;
						}
					}

					final SettingsProvider settingsProvider = context.getSettingsProvider();
					if (settingsProvider == null) return true; //If no settings, assume it's not filtered

					return settingsProvider.getGuildCommands(guild).getFilter().test(info.getPath());
				})
				.sorted(Comparator.comparingInt(info -> info.getPath().getNameCount()))
				.collect(Collectors.toCollection(ArrayList::new));
	}
}
