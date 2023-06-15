package com.freya02.botcommands.api.commands.application;

import com.freya02.botcommands.api.commands.CommandPath;
import com.freya02.botcommands.internal.commands.application.ApplicationCommandInfo;
import com.freya02.botcommands.internal.commands.application.CommandMap;
import com.freya02.botcommands.internal.commands.application.MutableCommandMap;
import com.freya02.botcommands.internal.commands.application.context.message.MessageCommandInfo;
import com.freya02.botcommands.internal.commands.application.context.user.UserCommandInfo;
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandInfo;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Map;

public abstract class ApplicationCommandMap {
	@UnmodifiableView
	public Collection<? extends ApplicationCommandInfo> getAllApplicationCommands() {
		return getRawTypeMap().values()
				.stream()
				.flatMap(map -> map.values().stream())
				.toList();
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
		return (CommandMap<T>) getRawTypeMap().computeIfAbsent(type, x -> new MutableCommandMap<>());
	}

	protected abstract Map<Command.Type, CommandMap<ApplicationCommandInfo>> getRawTypeMap();

	@NotNull
	public abstract ApplicationCommandMap plus(@NotNull ApplicationCommandMap liveApplicationCommandsMap);
}
