package com.freya02.botcommands.internal.application;

import com.freya02.botcommands.api.application.ApplicationCommandInfoMapView;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.internal.application.context.message.MessageCommandInfo;
import com.freya02.botcommands.internal.application.context.user.UserCommandInfo;
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import gnu.trove.TCollections;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;
import java.util.stream.Collectors;

public class ApplicationCommandsContextImpl implements ApplicationCommandsContext {
	private final ApplicationCommandInfoMap applicationCommandInfoMap = new ApplicationCommandInfoMap();
	private final TLongObjectMap<ApplicationCommandInfoMapView> liveApplicationCommandInfoMap = TCollections.synchronizedMap(new TLongObjectHashMap<>());

	private long getGuildKey(@Nullable Guild guild) {
		return guild == null ? 0 : guild.getIdLong();
	}

	@Nullable
	@Override
	public SlashCommandInfo findLiveSlashCommand(@Nullable Guild guild, @NotNull CommandPath path) {
		return liveApplicationCommandInfoMap.get(getGuildKey(guild)).findSlashCommand(path);
	}

	@Nullable
	@Override
	public UserCommandInfo findLiveUserCommand(@Nullable Guild guild, @NotNull String name) {
		return liveApplicationCommandInfoMap.get(getGuildKey(guild)).findUserCommand(name);
	}

	@Nullable
	@Override
	public MessageCommandInfo findLiveMessageCommand(@Nullable Guild guild, @NotNull String name) {
		return liveApplicationCommandInfoMap.get(getGuildKey(guild)).findMessageCommand(name);
	}

	@NotNull
	public ApplicationCommandInfoMap getApplicationCommandInfoMap() {
		return applicationCommandInfoMap;
	}

	@Override
	@NotNull
	@UnmodifiableView
	public ApplicationCommandInfoMapView getApplicationCommandInfoMapView() {
		return applicationCommandInfoMap;
	}

	@NotNull
	public CommandInfoMap<SlashCommandInfo> getSlashCommandsMap() {
		return getApplicationCommandInfoMap().getSlashCommands();
	}

	@Override
	@NotNull
	@UnmodifiableView
	public CommandInfoMap<SlashCommandInfo> getSlashCommandsMapView() {
		return getApplicationCommandInfoMapView().getSlashCommandsView();
	}

	@NotNull
	public CommandInfoMap<UserCommandInfo> getUserCommandsMap() {
		return getApplicationCommandInfoMap().getUserCommands();
	}

	@Override
	@NotNull
	@UnmodifiableView
	public CommandInfoMap<UserCommandInfo> getUserCommandsMapView() {
		return getApplicationCommandInfoMapView().getUserCommandsView();
	}

	@NotNull
	public CommandInfoMap<MessageCommandInfo> getMessageCommandsMap() {
		return getApplicationCommandInfoMap().getMessageCommands();
	}

	@Override
	@NotNull
	@UnmodifiableView
	public CommandInfoMap<MessageCommandInfo> getMessageCommandsMapView() {
		return getApplicationCommandInfoMapView().getMessageCommandsView();
	}

	@Override
	public List<CommandPath> getSlashCommandsPaths() {
		return getSlashCommandsMap().values()
				.stream()
				.map(SlashCommandInfo::getPath)
				.collect(Collectors.toList());
	}

	@Override
	@NotNull
	public ApplicationCommandInfoMapView getLiveApplicationCommandsMap(@Nullable Guild guild) {
		return liveApplicationCommandInfoMap.get(getGuildKey(guild));
	}

	public void putLiveApplicationCommandsMap(long guildId, @NotNull ApplicationCommandInfoMap map) {
		liveApplicationCommandInfoMap.put(guildId, map);
	}
}
