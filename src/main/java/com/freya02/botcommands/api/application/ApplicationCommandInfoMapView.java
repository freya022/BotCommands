package com.freya02.botcommands.api.application;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.CommandStatus;
import com.freya02.botcommands.api.SettingsProvider;
import com.freya02.botcommands.internal.application.ApplicationCommandInfo;
import com.freya02.botcommands.internal.application.CommandInfoMap;
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

public abstract class ApplicationCommandInfoMapView {
	protected final Map<Command.Type, CommandInfoMap<? extends ApplicationCommandInfo>> typeMap = Collections.synchronizedMap(new EnumMap<>(Command.Type.class));

	@UnmodifiableView
	public Collection<? extends ApplicationCommandInfo> getAllApplicationCommandsView() {
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
	public CommandInfoMap<SlashCommandInfo> getSlashCommandsView() {
		final CommandInfoMap<SlashCommandInfo> typeMap = getTypeMap(Command.Type.SLASH);

		return typeMap.unmodifiable();
	}

	@UnmodifiableView
	public CommandInfoMap<UserCommandInfo> getUserCommandsView() {
		final CommandInfoMap<UserCommandInfo> map = getTypeMap(Command.Type.USER);

		return map.unmodifiable();
	}

	@UnmodifiableView
	public CommandInfoMap<MessageCommandInfo> getMessageCommandsView() {
		final CommandInfoMap<MessageCommandInfo> map = getTypeMap(Command.Type.MESSAGE);

		return map.unmodifiable();
	}

	@Nullable
	public SlashCommandInfo findSlashCommand(@NotNull CommandPath path) {
		return this.getSlashCommandsView().get(path);
	}

	@Nullable
	public UserCommandInfo findUserCommand(@NotNull String name) {
		return this.getUserCommandsView().get(CommandPath.ofName(name));
	}

	@Nullable
	public MessageCommandInfo findMessageCommand(@NotNull String name) {
		return this.getMessageCommandsView().get(CommandPath.ofName(name));
	}

	@SuppressWarnings("unchecked")
	protected <T extends ApplicationCommandInfo> CommandInfoMap<T> getTypeMap(Command.Type type) {
		return (CommandInfoMap<T>) typeMap.computeIfAbsent(type, x -> new CommandInfoMap<>());
	}

	public List<ApplicationCommandInfo> filterByGuild(BContext context, Guild guild) {
		return getAllApplicationCommandsStream()
				.filter(info -> {
					if (info.isGuildOnly() && guild == null) { //Do not update guild-only commands in global context
						return false;
					} else if (!info.isGuildOnly() && guild != null) { //Do not update global commands in guild context
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

					if (info.getSpecificId() != null) {
						final CommandStatus commandStatus = info.getInstance().getGuildSpecificCommandStatus(context, guild, info.getSpecificId(), info.getPath());

						if (commandStatus == CommandStatus.DISABLED) return false;
					}

					final SettingsProvider settingsProvider = context.getSettingsProvider();
					if (settingsProvider == null) return true; //If no settings, assume it's not filtered

					final CommandStatus commandStatus = settingsProvider.getGuildSpecificCommandStatus(context, guild, info.getSpecificId(), info.getPath());
					if (commandStatus == CommandStatus.DISABLED) return false;

					return settingsProvider.getGuildCommands(guild).getFilter().test(info.getPath());
				})
				.sorted(Comparator.comparingInt(info -> info.getPath().getNameCount()))
				.collect(Collectors.toCollection(ArrayList::new));
	}
}
