package com.freya02.botcommands;

import com.freya02.botcommands.annotation.*;
import com.freya02.botcommands.regex.CommandTransformer;
import com.freya02.botcommands.regex.MethodPattern;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public final class CommandInfo {
	private final Command parentCommand;

	private final String name;
	private final String[] aliases;

	private final String description;

	private final String category;

	private final boolean requireOwner;
	private final boolean hidden;

	private final EnumSet<Permission> userPermissions = EnumSet.noneOf(Permission.class);
	private final EnumSet<Permission> botPermissions = EnumSet.noneOf(Permission.class);

	private final int cooldown;
	private final CooldownScope cooldownScope;

	private final boolean addSubcommandHelp, addExecutableHelp;

	private final List<MethodPattern> methodPatterns;
	private final List<Command> subcommands = new ArrayList<>();

	public CommandInfo(Command command, BContext context) {
		try {
			final Class<?> declaringClass = command.getClass().getDeclaringClass();
			if (declaringClass != null) {
				this.parentCommand = context.findCommand(declaringClass.getAnnotation(JdaCommand.class).name());
			} else {
				this.parentCommand = null;
			}

			final JdaCommand jdaCommand = command.getClass().getAnnotation(JdaCommand.class);
			category = jdaCommand.category();
			name = jdaCommand.name();

			if (name.contains(" "))
				throw new IllegalArgumentException("Command name cannot have spaces in '" + name + "'");

			aliases = jdaCommand.aliases();
			description = jdaCommand.description();

			if (parentCommand != null) {
				userPermissions.addAll(parentCommand.getInfo().getUserPermissions());
				botPermissions.addAll(parentCommand.getInfo().getBotPermissions());
			}

			Collections.addAll(userPermissions, jdaCommand.userPermissions());
			Collections.addAll(botPermissions, jdaCommand.botPermissions());

			cooldown = jdaCommand.cooldown();
			cooldownScope = jdaCommand.cooldownScope();

			methodPatterns = CommandTransformer.getMethodPatterns(command, command.getClass().isAnnotationPresent(DebugPatterns.class));

			hidden = command.getClass().isAnnotationPresent(Hidden.class) || (parentCommand != null && parentCommand.getInfo().isHidden());
			requireOwner = command.getClass().isAnnotationPresent(RequireOwner.class) || (parentCommand != null && parentCommand.getInfo().isRequireOwner());
			addSubcommandHelp = command.getClass().isAnnotationPresent(AddSubcommandHelp.class);
			addExecutableHelp = command.getClass().isAnnotationPresent(AddExecutableHelp.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Nullable
	public Command getSubcommand(@NotNull Command parent, @NotNull String name) {
		final List<Command> subcommands = parent.getInfo().getSubcommands();

		for (Command subcommand : subcommands) {
			if (subcommand.getInfo().getName().equals(name)) {
				return subcommand;
			}
		}

		return null;
	}

	void addSubcommand(Command command) {
		subcommands.add(command);
	}

	public List<Command> getSubcommands() {
		return subcommands;
	}

	public boolean isRequireOwner() {
		return requireOwner;
	}

	public Command getParentCommand() {
		return parentCommand;
	}

	public boolean isHidden() {
		return hidden;
	}

	public String getName() {
		return name;
	}

	public String[] getAliases() {
		return aliases;
	}

	public String getDescription() {
		return description;
	}

	public String getCategory() {
		return category;
	}

	public EnumSet<Permission> getUserPermissions() {
		return userPermissions;
	}

	public EnumSet<Permission> getBotPermissions() {
		return botPermissions;
	}

	public int getCooldown() {
		return cooldown;
	}

	public CooldownScope getCooldownScope() {
		return cooldownScope;
	}

	public boolean isAddSubcommandHelp() {
		return addSubcommandHelp;
	}

	public boolean isAddExecutableHelp() {
		return addExecutableHelp;
	}

	public List<MethodPattern> getMethodPatterns() {
		return methodPatterns;
	}
}