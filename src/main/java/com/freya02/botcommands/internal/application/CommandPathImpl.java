package com.freya02.botcommands.internal.application;

import com.freya02.botcommands.application.CommandPath;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.StringJoiner;

public class CommandPathImpl implements CommandPath {
	private final String name;
	private final String group;
	private final String subname;
	private final String path;
	private final int count;

	public CommandPathImpl(@Nonnull String name, @Nullable String group, @Nullable String subname) {
		Checks.notBlank(name, "Command base name");
		if (group != null) Checks.notBlank(group, "Subcommand group name");
		if (subname != null) Checks.notBlank(subname, "Subcommand name");
		
		this.name = name;
		this.group = group;
		this.subname = subname;
		
		final StringJoiner joiner = new StringJoiner("/");

		joiner.add(name);

		if (group != null)
			joiner.add(group);

		if (subname != null)
			joiner.add(subname);

		this.path = joiner.toString();

		int count = 1;
		if (group != null) count++;
		if (subname != null) count++;
		
		this.count = count;
	}

	@Nonnull
	@Override
	public String getName() {
		return name;
	}

	@Nullable
	@Override
	public String getGroup() {
		return group;
	}

	@Nullable
	@Override
	public String getSubname() {
		return subname;
	}

	@Override
	public int getNameCount() {
		return count;
	}

	@Nullable
	@Override
	public CommandPath getParent() {
		if (group != null && subname != null) { // /name group sub
			return CommandPath.of(name, group);
		} else if (group != null /*&& subname == null*/) { // /name group
			return CommandPath.of(name, group);
		} else if (subname != null) { // /name sub
			return CommandPath.of(name);
		} else { // /name
			return null;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CommandPathImpl that = (CommandPathImpl) o;

		return path.equals(that.path);
	}

	@Override
	public int hashCode() {
		return path.hashCode();
	}

	@Override
	public String getFullPath() {
		return path;
	}

	@Override
	public String toString() {
		return path;
	}
}
