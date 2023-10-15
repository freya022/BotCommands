package io.github.freya022.botcommands.api.commands;

import io.github.freya022.botcommands.internal.commands.CommandPathImpl;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a path of a command, each path component is delimited with a space, it is the same representation as JDA commands paths given in {@link SlashCommandInteractionEvent#getFullCommandName()}.
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
		return of(Arrays.asList(components));
	}

	@NotNull
	static CommandPath of(@NotNull List<@NotNull String> components) {
		if (components.size() == 1) {
			return new CommandPathImpl(components.get(0), null, null);
		} else if (components.size() == 2) {
			return new CommandPathImpl(components.get(0), null, components.get(1));
		} else if (components.size() == 3) {
			return new CommandPathImpl(components.get(0), components.get(1), components.get(2));
		} else {
			throw new IllegalArgumentException("Invalid path: '" + String.join(" ", components) + "'");
		}
	}

	/**
	 * Returns the top level name of this command path
	 * <br>For a slash command such as {@code /show me something}, this would be {@code show}
	 *
	 * @return Top level name of this command path
	 */
	@NotNull
	String getName();

	/**
	 * Returns the subcommand group name of this command path
	 * <br>For a slash command such as {@code /show me something}, this would be {@code me}
	 *
	 * @return Subcommand group name of this command path
	 */
	@Nullable
	String getGroup();

	/**
	 * Returns the subcommand name of this command path
	 * <br>For a slash command such as {@code /show me something}, this would be {@code something}
	 * <br>For a slash command such as {@code /tag info}, this would be {@code info}
	 *
	 * @return Subcommand name of this command path
	 */
	@Nullable
	String getSubname();

	/**
	 * Returns the number of path components of this command path
	 * <br>For a slash command such as {@code /show me something}, this would be {@code 3}
	 * <br>For a slash command such as {@code /tag info}, this would be {@code 2}
	 * <br>For a slash command such as {@code /say}, this would be {@code 1}
	 *
	 * @return The number of path components of this command path
	 */
	int getNameCount();

	/**
	 * Returns the parent path of this command path
	 * <br>For a slash command such as {@code /show me something}, this would be {@code /show me}
	 * <br>For a slash command such as {@code /tag info}, this would be {@code /tag}
	 * <br>For a slash command such as {@code /say}, this would be {@code null}
	 *
	 * @return The parent path of this command path
	 */
	@Nullable
	CommandPath getParent();

	/**
	 * Returns the full <i>encoded</i> path of this command path
	 * <br>Each path component is joined with a space delimiter
	 * <br>For a slash command such as {@code /show me something}, this would be {@code show me something}
	 *
	 * @return The full encoded path of this command path
	 */
	@NotNull
	String getFullPath();

	/**
	 * Returns the full path with the specified separator.
	 * <br>For a slash command such as {@code /show me something}, with a {@code -} separator,
	 * this would be {@code show-me-something}
	 *
	 * @return The full path with the specified separator
	 */
	@NotNull
	String getFullPath(char separator);

	/**
	 * Returns the right-most name of this command path
	 * <br>For a slash command such as {@code /show me something}, this would be {@code something}
	 *
	 * @return The right-most name of this command path
	 */
	@NotNull
	String getLastName();

	/**
	 * Return the name at the {@code i} index
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
	 * <br>For example {@code /show me something} starts with {@code /show me}
	 *
	 * @param o The other path to test against
	 * @return {@code true} if this path starts with the other, {@code false} otherwise
	 */
	boolean startsWith(CommandPath o);

	/**
	 * Indicates if this command path is equal to another object
	 *
	 * @param o Another object
	 * @return {@code true} if they are equal, {@code false} if not
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
