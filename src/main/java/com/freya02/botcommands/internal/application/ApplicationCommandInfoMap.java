package com.freya02.botcommands.internal.application;

import com.freya02.botcommands.api.application.ApplicationCommandInfoMapView;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.internal.application.context.message.MessageCommandInfo;
import com.freya02.botcommands.internal.application.context.user.UserCommandInfo;
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.function.Function;

public class ApplicationCommandInfoMap extends ApplicationCommandInfoMapView {
	public CommandInfoMap<SlashCommandInfo> getSlashCommands() {
		return getTypeMap(Command.Type.SLASH);
	}

	public CommandInfoMap<UserCommandInfo> getUserCommands() {
		return getTypeMap(Command.Type.USER);
	}

	public CommandInfoMap<MessageCommandInfo> getMessageCommands() {
		return getTypeMap(Command.Type.MESSAGE);
	}

	public <T extends ApplicationCommandInfo> T computeIfAbsent(Command.Type type, CommandPath path, Function<CommandPath, T> mappingFunction) {
		// it works :tm:
		return this.<T>getTypeMap(type).computeIfAbsent(path, mappingFunction);
	}

	public <T extends ApplicationCommandInfo> T put(Command.Type type, CommandPath path, T value) {
		// it works :tm:

		return this.<T>getTypeMap(type).put(path, value);
	}
}
