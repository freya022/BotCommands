package com.freya02.botcommands.api.application;

import com.freya02.botcommands.internal.application.CommandPathImpl;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a path of a command, each path component is delimited with a space, it is the same representation as JDA commands paths given in {@link SlashCommandInteractionEvent#getFullCommandName()}
 * <br>The different components are name, group and subcommand.
 * <br>This is mainly a utility class to avoid manipulating strings
 */
public interface CommandPath extends Comparable<CommandPath> {
	@NotNull
	static CommandPath of(@NotNull String name, @Nullable String group, @Nullable String subname) {
		return new CommandPathImpl(name, group, subname);
	}

	@NotNull
	static CommandPath of(@NotNull String name, @Nullable String subname) {
		return new CommandPathImpl(name, null, subname);
	}

	@NotNull
	static CommandPath ofName(@NotNull String name) {
		return new CommandPathImpl(name, null, null);
	}

	@NotNull
	static CommandPath of(@NotNull String path) {
		final String[] components = path.split(" ");
		for (String component : components) {
			Checks.matches(component, Checks.ALPHANUMERIC_WITH_DASH, "Path component");
		}

		return of(components);
	}

	@NotNull
	static CommandPath of(@NotNull String @NotNull ... components) {
		if (components.length == 1) {
			return new CommandPathImpl(components[0], null, null);
		} else if (components.length == 2) {
			return new CommandPathImpl(components[0], null, components[1]);
		} else if (components.length == 3) {
			return new CommandPathImpl(components[0], components[1], components[2]);
		} else {
			throw new IllegalArgumentException("Invalid path: '" + String.join(" ", components) + "'");
		}
	}

	/**
	 * Returns the top level name of this command path
	 * <br>For a slash command such as "<code>/show me something</code>", this would be "<code>show</code>"
	 *
	 * @return Top level name of this command path
	 */
	@NotNull
	String getName();

	/**
	 * Returns the subcommand group name of this command path
	 * <br>For a slash command such as "<code>/show me something</code>", this would be "<code>me</code>"
	 *
	 * @return Subcommand group name of this command path
	 */
	@Nullable
	String getGroup();

	/**
	 * Returns the subcommand name of this command path
	 * <br>For a slash command such as "<code>/show me something</code>", this would be "<code>something</code>"
	 * <br>For a slash command such as "<code>/tag info</code>", this would be "<code>info</code>"
	 *
	 * @return Subcommand name of this command path
	 */
	@Nullable
	String getSubname();

	/**
	 * Returns the number of path components of this command path
	 * <br>For a slash command such as "<code>/show me something</code>", this would be <code>3</code>
	 * <br>For a slash command such as "<code>/tag info</code>", this would be <code>2</code>
	 * <br>For a slash command such as "<code>/say</code>", this would be <code>1</code>
	 *
	 * @return The number of path components of this command path
	 */
	int getNameCount();

	/**
	 * Returns the parent path of this command path
	 * <br>For a slash command such as "<code>/show me something</code>", this would be "<code>/show me</code>"
	 * <br>For a slash command such as "<code>/tag info</code>", this would be "<code>/tag</code>"
	 * <br>For a slash command such as "<code>/say</code>", this would be <code>null</code>
	 *
	 * @return The parent path of this command path
	 */
	@Nullable
	CommandPath getParent();

	/**
	 * Returns the full <i>encoded</i> path of this command path
	 * <br>Each path component is joined with a space delimiter
	 * <br>For a slash command such as "<code>/show me something</code>", this would be "<code>show me something</code>"
	 *
	 * @return The full encoded path of this command path
	 */
	@NotNull
	String getFullPath();

	/**
	 * Returns the right-most name of this command path
	 * <br>For a slash command such as "<code>/show me something</code>", this would be "<code>something</code>"
	 *
	 * @return The right-most name of this command path
	 */
	@NotNull
	String getLastName();

	/**
	 * Return the name at the <code>i</code> index
	 *
	 * @param i The index of the name to get
	 * @return The name at the specified index
	 */
	@Nullable
	String getNameAt(int i);

	/**
	 * Returns the JDA path representation of this CommandPath
	 *
	 * @return The command path with / in between each component
	 */
	@NotNull
	String toString();

	/**
	 * Returns whether this command path starts with the supplied command path
	 * <br>For example "<code>/show me something</code>" starts with "<code>/show me</code>"
	 *
	 * @param o The other path to test against
	 * @return <code>true</code> if this path starts with the other, <code>false</code> otherwise
	 */
	boolean startsWith(CommandPath o);

	/**
	 * Indicates if this command path is equal to another object
	 *
	 * @param o Another object
	 * @return <code>true</code> if they are equal, <code>false</code> if not
	 */
	boolean equals(Object o);

	@Override
	default int compareTo(@NotNull CommandPath o) {
		if (this.getNameCount() == o.getNameCount()) {
			if (this.equals(o)) {
				return 0;
			}
		}

		return this.getFullPath().compareTo(o.getFullPath());
	}
}
