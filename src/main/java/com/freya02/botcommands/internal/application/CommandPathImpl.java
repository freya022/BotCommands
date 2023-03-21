package com.freya02.botcommands.internal.application;

import com.freya02.botcommands.api.application.CommandPath;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.StringJoiner;

public class CommandPathImpl implements CommandPath {
	private final String name;
	private final String group;
	private final String subname;
	private final String path;
	private final int count;

	public CommandPathImpl(@NotNull String name, @Nullable String group, @Nullable String subname) {
		Checks.notBlank(name, "Command base name");
		if (group != null) Checks.notBlank(group, "Subcommand group name");
		if (subname != null) Checks.notBlank(subname, "Subcommand name");
		
		this.name = name;
		this.group = group;
		this.subname = subname;
		
		final StringJoiner joiner = new StringJoiner(" ");

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

	@NotNull
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
			return CommandPath.ofName(name);
		} else if (subname != null) { // /name sub
			return CommandPath.ofName(name);
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

	@NotNull
	@Override
	public String getFullPath() {
		return path;
	}

	@NotNull
	@Override
	public String getFullPath(char separator) {
		return path.replace(' ', separator);
	}

	@NotNull
	@Override
	public String getLastName() {
		if (group != null) { //If a group exist, a subname exists, otherwise it's a bug.
			return subname;
		} else if (subname != null) {
			return subname;
		}

		return name;
	}

	@Override
	@Nullable
	public String getNameAt(int i) {
		return switch (i) {
			case 0 -> name;
			case 1 -> {
				if (group != null) { // /name group subname
					yield group;
				} else { // /name subname
					yield subname;
				}
			}
			case 2 -> subname;
			default -> throw new IllegalArgumentException("Invalid name count: " + i);
		};
	}

	@NotNull
	@Override
	public String toString() {
		return path;
	}

	@Override
	public boolean startsWith(CommandPath o) {
		if (o.getNameCount() > getNameCount()) return false;

		for (int i = 0; i < Math.min(getNameCount(), o.getNameCount()); i++) {
			if (!Objects.equals(o.getNameAt(i), getNameAt(i))) {
				return false;
			}
		}

		return true;
	}
}
