package com.freya02.botcommands.internal.application;

import com.freya02.botcommands.api.application.ApplicationCommandInfoMapView;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.internal.application.context.message.MessageCommandInfo;
import com.freya02.botcommands.internal.application.context.user.UserCommandInfo;
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;
import java.util.stream.Collectors;

public class ApplicationCommandsContextImpl implements ApplicationCommandsContext {
	private final ApplicationCommandInfoMap applicationCommandInfoMap = new ApplicationCommandInfoMap();

	@Nullable
	@Override
	public SlashCommandInfo findSlashCommand(@NotNull CommandPath path) {
		return getSlashCommandsMap().get(path);
	}

	@Nullable
	@Override
	public UserCommandInfo findUserCommand(@NotNull String name) {
		return getUserCommandsMap().get(CommandPath.ofName(name));
	}

	@Nullable
	@Override
	public MessageCommandInfo findMessageCommand(@NotNull String name) {
		return getMessageCommandsMap().get(CommandPath.ofName(name));
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
}
