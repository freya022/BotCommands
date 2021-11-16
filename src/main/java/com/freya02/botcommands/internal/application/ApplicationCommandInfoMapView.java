package com.freya02.botcommands.internal.application;

import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.internal.application.context.message.MessageCommandInfo;
import com.freya02.botcommands.internal.application.context.user.UserCommandInfo;
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import net.dv8tion.jda.api.interactions.commands.CommandType;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.EnumMap;

public abstract class ApplicationCommandInfoMapView {
	//Might or might not be localized
	protected final EnumMap<CommandType, CommandInfoMap<? extends ApplicationCommandInfo>> typeMap = new EnumMap<>(CommandType.class);

	@UnmodifiableView
	public Collection<? extends ApplicationCommandInfo> getAllApplicationCommandsView() {
		return typeMap.values()
				.stream()
				.flatMap(map -> map.values().stream())
				.toList();
	}

	public ApplicationCommandInfo get(CommandType type, CommandPath path) {
		return getTypeMap(type).get(path);
	}

	@UnmodifiableView
	public CommandInfoMap<SlashCommandInfo> getSlashCommandsView() {
		final CommandInfoMap<SlashCommandInfo> typeMap = getTypeMap(CommandType.SLASH);

		return typeMap.unmodifiable();
	}

	@UnmodifiableView
	public CommandInfoMap<UserCommandInfo> getUserCommandsView() {
		final CommandInfoMap<UserCommandInfo> map = getTypeMap(CommandType.USER_CONTEXT);

		return map.unmodifiable();
	}

	@UnmodifiableView
	public CommandInfoMap<MessageCommandInfo> getMessageCommandsView() {
		final CommandInfoMap<MessageCommandInfo> map = getTypeMap(CommandType.MESSAGE_CONTEXT);

		return map.unmodifiable();
	}

	@SuppressWarnings("unchecked")
	protected <T extends ApplicationCommandInfo> CommandInfoMap<T> getTypeMap(CommandType type) {
		return (CommandInfoMap<T>) typeMap.computeIfAbsent(type, x -> new CommandInfoMap<>());
	}
}
