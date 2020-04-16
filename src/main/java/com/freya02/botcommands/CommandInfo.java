package com.freya02.botcommands;

import net.dv8tion.jda.api.Permission;

import java.util.List;

public final class CommandInfo {
	private final Command command;

	private final String name;
	private final String[] aliases;

	private final String description;
	private final String category;

	private final boolean requireOwner;
	private final boolean hidden;

	private final Permission[] userPermissions;
	private final Permission[] botPermissions;

	private final String requiredRole;

	private final int cooldown;
	private final CooldownScope cooldownScope;

	private final List<CommandInfo> subcommandsInfo;

	private final boolean addSubcommandHelp;

	public CommandInfo(Command command, String name, String[] aliases, String description, String category,
	                   boolean hidden, boolean requireOwner,
	                   Permission[] userPermissions, Permission[] botPermissions,
	                   String requiredRole,
	                   int cooldown, CooldownScope cooldownScope,
	                   List<CommandInfo> subcommandsInfo, boolean addSubcommandHelp) {
		this.command = command;
		this.name = name;
		this.aliases = aliases;
		this.description = description;
		this.category = category;
		this.hidden = hidden;
		this.requireOwner = requireOwner;
		this.userPermissions = userPermissions;
		this.botPermissions = botPermissions;
		this.requiredRole = requiredRole;
		this.cooldown = cooldown;
		this.cooldownScope = cooldownScope;
		this.subcommandsInfo = subcommandsInfo;
		this.addSubcommandHelp = addSubcommandHelp;
	}

	public boolean isRequireOwner() {
		return requireOwner;
	}

	public boolean isHidden() {
		return hidden;
	}

	public Command getCommand() {
		return command;
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

	public Permission[] getUserPermissions() {
		return userPermissions;
	}

	public Permission[] getBotPermissions() {
		return botPermissions;
	}

	public String getRequiredRole() {
		return requiredRole;
	}

	public int getCooldown() {
		return cooldown;
	}

	public CooldownScope getCooldownScope() {
		return cooldownScope;
	}

	public List<CommandInfo> getSubcommandsInfo() {
		return subcommandsInfo;
	}

	public boolean isAddSubcommandHelp() {
		return addSubcommandHelp;
	}
}