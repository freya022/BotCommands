package com.freya02.botcommands.api.application;

import com.freya02.botcommands.internal.application.CommandPathImpl;
import net.dv8tion.jda.api.events.interaction.commands.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a path of a command, each path component is delimited with a /, it is the same representation as JDA commands paths given in {@link SlashCommandEvent#getCommandPath()}
 * <br>The different components are name, group and subcommand.
 * <br>This is mainly a utility class to avoid manipulating strings
 */
public interface CommandPath {
	static CommandPath of(@NotNull String name, @Nullable String group, @Nullable String subname) {
		return new CommandPathImpl(name, group, subname);
	}

	static CommandPath of(@NotNull String name, @Nullable String subname) {
		return new CommandPathImpl(name, null, subname);
	}

	static CommandPath ofName(@NotNull String name) {
		return new CommandPathImpl(name, null, null);
	}

	static CommandPath of(@NotNull String path) {
		final String[] components = path.split("/");

		return of(components);
	}

	static CommandPath of(String... components) {
		if (components.length == 1) {
			return new CommandPathImpl(components[0], null, null);
		} else if (components.length == 2) {
			return new CommandPathImpl(components[0], null, components[1]);
		} else if (components.length == 3) {
			return new CommandPathImpl(components[0], components[1], components[2]);
		} else {
			throw new IllegalArgumentException("Invalid path: '" + String.join("/", components) + "'");
		}
	}

	@NotNull
	String getName();

	@Nullable
	String getGroup();

	@Nullable
	String getSubname();

	int getNameCount();
	
	@Nullable
	CommandPath getParent();
	
	String getFullPath();

	@NotNull
	String getLastName();

	/**
	 * Returns the JDA path representation of this CommandPath
	 *
	 * @return The command path with / in between each component
	 */
	String toString();

	boolean startsWith(CommandPath o);
}
