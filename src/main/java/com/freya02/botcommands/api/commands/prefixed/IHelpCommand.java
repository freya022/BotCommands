package com.freya02.botcommands.api.commands.prefixed;

import com.freya02.botcommands.internal.commands.prefixed.TextCommandInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Interface which needs to be implemented by the help command.
 * <br>This lets the framework use the command to also display help about specific commands
 *
 * <p>You can implement a help command just like a normal command, but it has to implement this interface.
 */
public interface IHelpCommand {
	/**
	 * Is fired when a command is recognized, but the arguments cannot be resolved on any of the command variants.
	 *
	 * <p><b>Be aware: </b>Localization annotations cannot be applied on this method
	 *
	 * @param event        The event of the current command invocation
	 * @param commandInfos The command data of the command which the user tried to use, they share the same path, but have different content
	 */
	void onInvalidCommand(@NotNull BaseCommandEvent event, @NotNull Collection<TextCommandInfo> commandInfos);
}
