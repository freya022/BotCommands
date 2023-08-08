package com.freya02.botcommands.api.commands.prefixed;

import com.freya02.botcommands.api.core.config.BServiceConfigBuilder;
import com.freya02.botcommands.api.core.service.annotations.BService;
import com.freya02.botcommands.api.core.service.annotations.InterfacedService;
import com.freya02.botcommands.internal.commands.prefixed.TextCommandInfo;
import org.jetbrains.annotations.NotNull;

/**
 * Interface which needs to be implemented by the help command.
 * <br>This lets the framework use the command to also display help about specific commands
 *
 * <p>You can implement a help command just like a normal command, but it has to implement this interface.
 *
 * <p>
 * <b>Usage</b>: Register your instance as a service with {@link BService}
 * or {@link BServiceConfigBuilder#getServiceAnnotations() any annotation that enables your class for dependency injection}.
 */
@InterfacedService(acceptMultiple = false)
public interface IHelpCommand {
	/**
	 * Is fired when a command is recognized, but the arguments cannot be resolved on any of the command variants.
	 *
	 * <p><b>Be aware: </b>Localization annotations cannot be applied on this method
	 *
	 * @param event       The event of the current command invocation
	 * @param commandInfo The command info of the command which the user tried to use
	 */
	void onInvalidCommand(@NotNull BaseCommandEvent event, @NotNull TextCommandInfo commandInfo);
}
