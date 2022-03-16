package com.freya02.botcommands.internal.application;

import com.freya02.botcommands.api.application.ApplicationCommandInfoMapView;
import com.freya02.botcommands.api.application.ApplicationCommandsContext;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.internal.application.context.message.MessageCommandInfo;
import com.freya02.botcommands.internal.application.context.user.UserCommandInfo;
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.modals.ModalHandlerInfo;
import com.freya02.botcommands.internal.utils.Utils;
import gnu.trove.TCollections;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ApplicationCommandsContextImpl implements ApplicationCommandsContext {
	private final ApplicationCommandInfoMap applicationCommandInfoMap = new ApplicationCommandInfoMap();
	private final TLongObjectMap<ApplicationCommandInfoMapView> liveApplicationCommandInfoMap = TCollections.synchronizedMap(new TLongObjectHashMap<>());

	private final Map<String, ModalHandlerInfo> modalHandlersMap = new HashMap<>();

	private long getGuildKey(@Nullable Guild guild) {
		return guild == null ? 0 : guild.getIdLong();
	}

	@Nullable
	@Override
	public SlashCommandInfo findLiveSlashCommand(@Nullable Guild guild, @NotNull CommandPath path) {
		final ApplicationCommandInfoMapView view = liveApplicationCommandInfoMap.get(getGuildKey(guild));
		if (view == null) return null;

		return view.findSlashCommand(path);
	}

	@Nullable
	@Override
	public UserCommandInfo findLiveUserCommand(@Nullable Guild guild, @NotNull String name) {
		final ApplicationCommandInfoMapView view = liveApplicationCommandInfoMap.get(getGuildKey(guild));
		if (view == null) return null;

		return view.findUserCommand(name);
	}

	@Nullable
	@Override
	public MessageCommandInfo findLiveMessageCommand(@Nullable Guild guild, @NotNull String name) {
		final ApplicationCommandInfoMapView view = liveApplicationCommandInfoMap.get(getGuildKey(guild));
		if (view == null) return null;

		return view.findMessageCommand(name);
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

	public void putLiveApplicationCommandsMap(@Nullable Guild guild, @NotNull ApplicationCommandInfoMap map) {
		liveApplicationCommandInfoMap.put(getGuildKey(guild), map);
	}

	public void addModalHandler(ModalHandlerInfo handlerInfo) {
		final ModalHandlerInfo oldHandler = modalHandlersMap.put(handlerInfo.getHandlerName(), handlerInfo);

		if (oldHandler != null) {
			throw new IllegalArgumentException("Tried to register modal handler '%s' at %s but it was already registered at %s".formatted(handlerInfo.getHandlerName(),
					Utils.formatMethodShort(handlerInfo.getMethod()),
					Utils.formatMethodShort(oldHandler.getMethod()))
			);
		}
	}

	@Nullable
	public ModalHandlerInfo getModalHandler(String handlerName) {
		return modalHandlersMap.get(handlerName);
	}
}
