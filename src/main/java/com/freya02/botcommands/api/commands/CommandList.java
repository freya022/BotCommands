package com.freya02.botcommands.api.commands;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Represents a list of enabled commands, the enabled commands are only qualified by their base name<br>
 * <b>Keep in mind you cannot disable global commands on a per-guild basis</b>
 */
public class CommandList {
	private final Predicate<CommandPath> filter;

	private CommandList(Predicate<CommandPath> filter) {
		this.filter = filter;
	}

	/**
	 * Makes a list of <b>usable</b> commands (in a Guild context),
	 * <br><b>You have to insert full commands paths such as <code>name group subcommand</code>, which comes from the Discord representation of <code>/name group subcommand</code></b>
	 * <br>This is constructed by joining each path component with a space
	 * <br><b>Keep in mind you cannot enable global commands on a per-guild basis</b>
	 *
	 * @param enabledCommandsStrs A collection of enabled command paths
	 * @return A {@link CommandList} of enabled command
	 */
	public static CommandList of(Collection<String> enabledCommandsStrs) {
		List<CommandPath> enabledCommands = enabledCommandsStrs.stream().map(CommandPath::of).collect(Collectors.toList());

		return new CommandList(path -> containsCommand(enabledCommands, path));
	}

	private static boolean containsCommand(List<CommandPath> commandOrGroupPaths, CommandPath path) {
		for (CommandPath commandOrGroupPath : commandOrGroupPaths) {
			if (path.startsWith(commandOrGroupPath)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Makes a list of <b>unusable</b> commands (in a Guild context),
	 * <br><b>You have to insert full commands paths such as <code>name group subcommand</code>, which comes from the Discord representation of <code>/name group subcommand</code></b>
	 * <br>This is constructed by joining each path component with a space
	 * <br><b>Keep in mind you cannot disable global commands on a per-guild basis</b>
	 *
	 * @param disabledCommandsStrs A collection of disabled command paths
	 * @return A {@link CommandList} of disabled command
	 */
	public static CommandList notOf(Collection<String> disabledCommandsStrs) {
		List<CommandPath> disabledCommands = disabledCommandsStrs.stream().map(CommandPath::of).collect(Collectors.toList());

		return new CommandList(path -> !containsCommand(disabledCommands, path));
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

	/**
	 * Makes a list that enables current guild's commands if they satisfy the given predicate
	 *
	 * @param predicate The CommandPath predicate, returns <code>true</code> if the command can be used
	 * @return The CommandList for this predicate
	 */
	public static CommandList filter(Predicate<CommandPath> predicate) {
		return new CommandList(predicate);
	}

	/**
	 * Return a predicate that returns true if the command can be used
	 *
	 * @return The filter predicate
	 */
	public Predicate<CommandPath> getFilter() {
		return filter;
	}
}
