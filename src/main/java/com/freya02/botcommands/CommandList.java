package com.freya02.botcommands;

import java.util.Collection;
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
	 * Makes a list of <b>usable</b> commands (in a Guild context),
	 * <br><b>You have to insert full commands paths such as <code>name/group/subcommand</code>, which comes from the Discord representation of <code>/name group subcommand</code></b>
	 * <br>This is constructed by joining each path component with a <code>/</code>
	 * <br><b>Keep in mind you cannot enable global commands on a per-guild basis</b>
	 *
	 * @param enabledCommands A collection of enabled command paths
	 * @return A {@link CommandList} of enabled command
	 */
	public static CommandList of(Collection<String> enabledCommands) {
		return new CommandList(enabledCommands::contains);
	}

	/**
	 * Makes a list of <b>unusable</b> commands (in a Guild context),
	 * <br><b>You have to insert full commands paths such as <code>name/group/subcommand</code>, which comes from the Discord representation of <code>/name group subcommand</code></b>
	 * <br>This is constructed by joining each path component with a <code>/</code>
	 * <br><b>Keep in mind you cannot disable global commands on a per-guild basis</b>
	 *
	 * @param disabledCommands A collection of disabled command paths
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

	/**
	 * Return a predicate that returns true if the command can be used
	 *
	 * @return The filter predicate
	 */
	public Predicate<String> getFilter() {
		return filter;
	}
}
