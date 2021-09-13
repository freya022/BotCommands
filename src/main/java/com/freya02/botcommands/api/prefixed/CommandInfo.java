package com.freya02.botcommands.api.prefixed;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.annotations.DontInheritPermissions;
import com.freya02.botcommands.api.annotations.RequireOwner;
import com.freya02.botcommands.api.prefixed.annotations.AddExecutableHelp;
import com.freya02.botcommands.api.prefixed.annotations.AddSubcommandHelp;
import com.freya02.botcommands.api.prefixed.annotations.Hidden;
import com.freya02.botcommands.api.prefixed.annotations.JdaCommand;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.Cooldownable;
import com.freya02.botcommands.internal.prefixed.regex.CommandTransformer;
import com.freya02.botcommands.internal.prefixed.regex.MethodPattern;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public final class CommandInfo extends Cooldownable {
	private final Command parentCommand;

	private final String name;
	private final String[] aliases;

	private final String description;

	private final String category;

	private final boolean requireOwner;
	private final boolean hidden;

	private final EnumSet<Permission> userPermissions = EnumSet.noneOf(Permission.class);
	private final EnumSet<Permission> botPermissions = EnumSet.noneOf(Permission.class);

	private final boolean addSubcommandHelp, addExecutableHelp;

	private final List<MethodPattern> methodPatterns;
	private final List<Command> subcommands = new ArrayList<>();

	public CommandInfo(Command command, BContext context) {
		super(command.getClass().getAnnotation(JdaCommand.class).cooldownScope(), command.getClass().getAnnotation(JdaCommand.class).cooldown());

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

			if (parentCommand != null && !command.getClass().isAnnotationPresent(DontInheritPermissions.class)) {
				userPermissions.addAll(parentCommand.getInfo().getUserPermissions());
				botPermissions.addAll(parentCommand.getInfo().getBotPermissions());
			}

			Collections.addAll(userPermissions, jdaCommand.userPermissions());
			Collections.addAll(botPermissions, jdaCommand.botPermissions());

			methodPatterns = CommandTransformer.getMethodPatterns(command);

			hidden = command.getClass().isAnnotationPresent(Hidden.class) || (parentCommand != null && parentCommand.getInfo().isHidden());
			requireOwner = command.getClass().isAnnotationPresent(RequireOwner.class) || (parentCommand != null && parentCommand.getInfo().isOwnerRequired());
			addSubcommandHelp = ((BContextImpl) context).shouldAddSubcommandHelpByDefault()
					|| command.getClass().isAnnotationPresent(AddSubcommandHelp.class);
			addExecutableHelp = ((BContextImpl) context).shouldAddExecutableHelpByDefault()
					|| command.getClass().isAnnotationPresent(AddExecutableHelp.class);
		} catch (Exception e) {
			throw new RuntimeException("Unable to construct command info for " + command, e);
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

	public void addSubcommand(Command command) {
		subcommands.add(command);
	}

	public List<Command> getSubcommands() {
		return subcommands;
	}

	public boolean isOwnerRequired() {
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