package com.freya02.botcommands.application;

import com.freya02.botcommands.internal.application.CommandPathImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface CommandPath {
	static CommandPath of(@Nonnull String name, @Nullable String group, @Nullable String subname) {
		return new CommandPathImpl(name, group, subname);
	}

	static CommandPath of(@Nonnull String name, @Nullable String subname) {
		return new CommandPathImpl(name, null, subname);
	}

	static CommandPath ofName(@Nonnull String name) {
		return new CommandPathImpl(name, null, null);
	}

	static CommandPath of(@Nonnull String path) {
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

	@Nonnull
	String getName();

	@Nullable
	String getGroup();

	@Nullable
	String getSubname();

	int getNameCount();
	
	@Nullable
	CommandPath getParent();
	
	String getFullPath();

	/**
	 * Returns the JDA path representation of this CommandPath
	 *
	 * @return The command path with / in between each component
	 */
	String toString();

	boolean startsWith(CommandPath o);
}
