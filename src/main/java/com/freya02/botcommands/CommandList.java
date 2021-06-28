package com.freya02.botcommands;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * Represents a list of enabled commands, the enabled commands are only qualified by their base name<br>
 * <b>Keep in mind you cannot disable global commands on a per-guild basis</b>
 */
public class CommandList {
	private final Predicate<String> filter;

	CommandList(Predicate<String> filter) {
		this.filter = filter;
	}

	/**
	 * Makes a list of <b>usable</b> commands (in a Guild context), <b>insert only base command name (most left name), no group, no subcommand</b><br>
	 * <b>Keep in mind you cannot disable global commands on a per-guild basis</b>
	 *
	 * @param enabledCommands A collection of enabled command base names
	 * @return A {@link CommandList} of enabled command
	 */
	public static CommandList of(Collection<String> enabledCommands) {
		return new CommandList(enabledCommands::contains);
	}

	/**
	 * Makes a list of <b>unusable</b> commands (in a Guild context), <b>insert only base command name (most left name), no group, no subcommand</b><br>
	 * <b>Keep in mind you cannot disable global commands on a per-guild basis</b>
	 *
	 * @param disabledCommands A collection of disabled command base names
	 * @return A {@link CommandList} of disabled command
	 */
	public static CommandList notOf(Collection<String> disabledCommands) {
		return new CommandList(s -> !disabledCommands.contains(s));
	}

	/**
	 * Makes a list that disables all of this current guild's commands
	 *
	 * @return A {@link CommandList} with no command enabled
	 */
	public static CommandList none() {
		return new CommandList(s -> false);
	}

	/**
	 * Makes a list that enables all of this current guild's commands
	 *
	 * @return A {@link CommandList} with all commands enabled
	 */
	public static CommandList all() {
		return new CommandList(s -> true);
	}

	public List<CommandData> getFiltered(Collection<CommandData> commandList) {
		List<CommandData> newCommandList = new ArrayList<>();

		for (CommandData commandData : commandList) {
			if (filter.test(commandData.getName())) {
				newCommandList.add(commandData);
			}
		}

		return newCommandList;
	}
}
