package com.freya02.botcommands.internal.application;

import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.internal.application.context.message.MessageCommandInfo;
import com.freya02.botcommands.internal.application.context.user.UserCommandInfo;
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import net.dv8tion.jda.api.interactions.commands.CommandType;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.function.Function;

public class ApplicationCommandInfoMap {
	public static class CommandInfoMap<T extends ApplicationCommandInfo> extends HashMap<CommandPath, T> {}

	//Might or might not be localized
	private final EnumMap<CommandType, CommandInfoMap<? extends ApplicationCommandInfo>> typeMap = new EnumMap<>(CommandType.class);
	
	public Collection<? extends ApplicationCommandInfo> getAllApplicationCommands() {
		return typeMap.values()
				.stream()
				.flatMap(map -> map.values().stream())
				.toList();
	}

	public <T extends ApplicationCommandInfo> T computeIfAbsent(CommandType type, CommandPath path, Function<CommandPath, T> mappingFunction) {
		// it works :tm:
		return this.<T>getTypeMap(type).computeIfAbsent(path, mappingFunction);
	}

	public <T extends ApplicationCommandInfo> T put(CommandType type, CommandPath path, T value) {
		// it works :tm:

		return this.<T>getTypeMap(type).put(path, value);
	}

	public ApplicationCommandInfo get(CommandType type, CommandPath path) {
		return getTypeMap(type).get(path);
	}

	public CommandInfoMap<SlashCommandInfo> getSlashCommands() {
		return getTypeMap(CommandType.SLASH);
	}

	public CommandInfoMap<UserCommandInfo> getUserCommands() {
		return getTypeMap(CommandType.USER_CONTEXT);
	}

	public CommandInfoMap<MessageCommandInfo> getMessageCommands() {
		return getTypeMap(CommandType.MESSAGE_CONTEXT);
	}

	@SuppressWarnings("unchecked")
	private <T extends ApplicationCommandInfo> CommandInfoMap<T> getTypeMap(CommandType type) {
		return (CommandInfoMap<T>) typeMap.computeIfAbsent(type, x -> new CommandInfoMap<>());
	}
}
