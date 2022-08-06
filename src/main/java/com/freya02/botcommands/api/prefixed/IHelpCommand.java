package com.freya02.botcommands.api.prefixed;

import com.freya02.botcommands.api.application.CommandPath;
import org.jetbrains.annotations.NotNull;

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
	 * @param event               The event of the current command invocation
	 * @param executedCommandPath The command path of the command which tried to be used
	 */
	void onInvalidCommand(@NotNull BaseCommandEvent event, @NotNull CommandPath executedCommandPath);
}
