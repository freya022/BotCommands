package com.freya02.botcommands.slash;

import com.freya02.botcommands.BContext;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * Every slash command has to inherit this class
 */
public abstract class SlashCommand {
	public SlashCommand() {}

	public SlashCommand(BContext context) { }

	/**
	 * Retrieves the choices available for this command path
	 * <br>A command path is the complete name, a slash command displayed as <code>/name group subcommand</code> on Discord would be translated into <code>name/group/subcommand</code>
	 *
	 * @param guild          The Guild in which the command is, each guild will have their own choices
	 * @param cmdPath        The path of the command such as <code>name/group/subcommand</code>
	 * @param optionName     The <b>Discord name of the option</b> (such as <code>delete_days</code>), do not confuse with the parameter name of your method
	 * @param parameterIndex The index of the method parameter, should start from 1 (as parameter 0 is the event)
	 * @return A collection of choices for the specified command path in the specified {@link Guild}, the list can be empty to add no choices
	 */
	public Collection<Command.Choice> getChoices(@Nullable Guild guild, @NotNull String cmdPath, @NotNull String optionName, int parameterIndex) {
		return Collections.emptyList();
	}
}
