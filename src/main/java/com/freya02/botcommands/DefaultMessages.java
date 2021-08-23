package com.freya02.botcommands;

import com.freya02.botcommands.internal.utils.Utils;

import java.util.StringJoiner;

public final class DefaultMessages {
	public static final String DEFAULT_USER_PERM_ERROR_MESSAGE = "You are not allowed to do this";
	public static final String DEFAULT_BOT_PERM_ERROR_MESSAGE = "I am missing these permissions: %s";
	public static final String DEFAULT_OWNER_ONLY_ERROR_MESSAGE = "Only the owner can use this";
	public static final String DEFAULT_USER_COOLDOWN_MESSAGE = "You must wait **%.2f seconds**";
	public static final String DEFAULT_CHANNEL_COOLDOWN_MESSAGE = "You must wait **%.2f seconds in this channel**";
	public static final String DEFAULT_GUILD_COOLDOWN_MESSAGE = "You must wait **%.2f seconds in this guild**";
	public static final String DEFAULT_COMMAND_NOT_FOUND_MESSAGE = "Unknown command, maybe you meant: %s";
	public static final String DEFAULT_COMMAND_ERROR_MESSAGE = "An uncaught exception occurred";
	public static final String DEFAULT_CLOSED_DM_ERROR_MESSAGE = "This component is not usable anymore";
	public static final String DEFAULT_APPLICATION_COMMAND_NOT_FOUND_MESSAGE = "Unknown application command";
	public static final String DEFAULT_APPLICATION_COMMAND_ERROR_MESSAGE = "An uncaught exception occurred";
	public static final String DEFAULT_SLASH_COMMAND_UNRESOLVABLE_PARAMETER_MESSAGE = "The parameter '%s' could not be resolved into a %s";
	public static final String DEFAULT_SLASH_COMMAND_INVALID_PARAMETER_TYPE_MESSAGE = "The parameter '%s' is not a valid type (expected a %s, got a %s)";
	public static final String DEFAULT_NULL_COMPONENT_TYPE_ERROR_MESSAGE = "This component is not usable anymore";

	private String userPermErrorMsg = DEFAULT_USER_PERM_ERROR_MESSAGE;
	private String botPermErrorMsg = DEFAULT_BOT_PERM_ERROR_MESSAGE;
	private String ownerOnlyErrorMsg = DEFAULT_OWNER_ONLY_ERROR_MESSAGE;

	private String userCooldownMsg = DEFAULT_USER_COOLDOWN_MESSAGE;
	private String channelCooldownMsg = DEFAULT_CHANNEL_COOLDOWN_MESSAGE;
	private String guildCooldownMsg = DEFAULT_GUILD_COOLDOWN_MESSAGE;

	private String commandNotFoundMsg = DEFAULT_COMMAND_NOT_FOUND_MESSAGE;
	private String commandErrorMsg = DEFAULT_COMMAND_ERROR_MESSAGE;
	private String closedDMErrorMsg = DEFAULT_CLOSED_DM_ERROR_MESSAGE;
	private String applicationCommandNotFoundMsg = DEFAULT_APPLICATION_COMMAND_NOT_FOUND_MESSAGE;
	private String applicationCommandErrorMsg = DEFAULT_APPLICATION_COMMAND_ERROR_MESSAGE;
	private String slashCommandUnresolvableParameterMsg = DEFAULT_SLASH_COMMAND_UNRESOLVABLE_PARAMETER_MESSAGE;
	private String slashCommandInvalidParameterTypeMsg = DEFAULT_SLASH_COMMAND_INVALID_PARAMETER_TYPE_MESSAGE;
	private String nullComponentTypeErrorMsg = DEFAULT_NULL_COMPONENT_TYPE_ERROR_MESSAGE;

	private static String checkFormatters(String str, String... formatters) {
		int start = 0;

		for (String formatter : formatters) {
			final int index = str.indexOf(formatter, start);
			if (index == -1) { //Formatter not found or beyond bounds
				final StringJoiner joiner = new StringJoiner("', ", "'", "'");
				for (String s : formatters) {
					joiner.add(s);
				}

				throw new IllegalArgumentException(String.format("String '%s' must have these formatters in order: %s", str, joiner));
			} else {
				start = index + formatter.length();
			}
		}

		return str;
	}

	public String getUserPermErrorMsg() {
		return userPermErrorMsg;
	}

	/**
	 * <p>Sets the displayed message when the user does not have enough permissions</p>
	 * <p><i>Default message : {@value #DEFAULT_USER_PERM_ERROR_MESSAGE}</i></p>
	 *
	 * @param userPermErrorMsg Message to display when the user does not have enough permissions
	 * @return This object for chaining convenience
	 */
	public DefaultMessages setUserPermErrorMsg(String userPermErrorMsg) {
		this.userPermErrorMsg = Utils.requireNonBlank(userPermErrorMsg, "User permission error message");

		return this;
	}

	public String getBotPermErrorMsg() {
		return botPermErrorMsg;
	}

	/**
	 * <p>Sets the displayed message when the bot does not have enough permissions</p>
	 * <b>The message must have a %s format specifier to insert the needed permissions</b>
	 * <p><i>Default message : {@value #DEFAULT_BOT_PERM_ERROR_MESSAGE}</i></p>
	 *
	 * @param botPermErrorMsg Message to display when the bot does not have enough permissions
	 * @return This object for chaining convenience
	 */
	public DefaultMessages setBotPermErrorMsg(String botPermErrorMsg) {
		Utils.requireNonBlank(botPermErrorMsg, "Bot permission error message");
		this.botPermErrorMsg = checkFormatters(botPermErrorMsg, "%s");

		return this;
	}

	public String getOwnerOnlyErrorMsg() {
		return ownerOnlyErrorMsg;
	}

	/**
	 * <p>Sets the displayed message when the command is only usable by the owner</p>
	 * <p><i>Default message : {@value #DEFAULT_OWNER_ONLY_ERROR_MESSAGE}</i></p>
	 *
	 * @param ownerOnlyErrorMsg Message to display when the command is only usable by the owner
	 * @return This object for chaining convenience
	 */
	public DefaultMessages setOwnerOnlyErrorMsg(String ownerOnlyErrorMsg) {
		this.ownerOnlyErrorMsg = Utils.requireNonBlank(ownerOnlyErrorMsg, "Owner only error message");

		return this;
	}

	public String getUserCooldownMsg() {
		return userCooldownMsg;
	}

	/**
	 * <p>Sets the displayed message when the command is on per-user cooldown</p>
	 * <p><b>Requires one string format for the per-user cooldown time (in seconds)</b></p>
	 * <p><i>Default message : {@value #DEFAULT_USER_COOLDOWN_MESSAGE}</i></p>
	 *
	 * @param userCooldownMsg Message to display when the command is on per-user cooldown
	 * @return This object for chaining convenience
	 */
	public DefaultMessages setUserCooldownMsg(String userCooldownMsg) {
		Utils.requireNonBlank(userCooldownMsg, "User cooldown error message");
		this.userCooldownMsg = checkFormatters(userCooldownMsg, "%.2f");

		return this;
	}

	public String getChannelCooldownMsg() {
		return channelCooldownMsg;
	}

	/**
	 * <p>Sets the displayed message when the command is on per-channel cooldown</p>
	 * <p><b>Requires one string format for the per-channel cooldown time (in seconds)</b></p>
	 * <p><i>Default message : {@value #DEFAULT_CHANNEL_COOLDOWN_MESSAGE}</i></p>
	 *
	 * @param channelCooldownMsg Message to display when the command is on per-channel cooldown
	 * @return This object for chaining convenience
	 */
	public DefaultMessages setChannelCooldownMsg(String channelCooldownMsg) {
		Utils.requireNonBlank(channelCooldownMsg, "Channel cooldown error message");
		this.channelCooldownMsg = checkFormatters(channelCooldownMsg, "%.2f");

		return this;
	}

	public String getGuildCooldownMsg() {
		return guildCooldownMsg;
	}

	/**
	 * <p>Sets the displayed message when the command is on per-guild cooldown</p>
	 * <p><b>Requires one string format for the per-guild cooldown time (in seconds)</b></p>
	 * <p><i>Default message : {@value #DEFAULT_GUILD_COOLDOWN_MESSAGE}</i></p>
	 *
	 * @param guildCooldownMsg Message to display when the command is on per-guild cooldown
	 * @return This object for chaining convenience
	 */
	public DefaultMessages setGuildCooldownMsg(String guildCooldownMsg) {
		Utils.requireNonBlank(guildCooldownMsg, "Guild cooldown error message");
		this.guildCooldownMsg = checkFormatters(guildCooldownMsg, "%.2f");

		return this;
	}

	public String getCommandNotFoundMsg() {
		return commandNotFoundMsg;
	}

	/**
	 * <p>Sets the displayed message when the command is not found</p>
	 * <b>The message must have a %s format specifier to insert the suggested commands</b>
	 * <p><i>Default message : {@value #DEFAULT_COMMAND_NOT_FOUND_MESSAGE}</i></p>
	 *
	 * @param commandNotFoundMsg Message to display when the command is not found
	 * @return This object for chaining convenience
	 */
	public DefaultMessages setCommandNotFoundMsg(String commandNotFoundMsg) {
		Utils.requireNonBlank(commandNotFoundMsg, "'Command not found' error message");
		this.commandNotFoundMsg = checkFormatters(commandNotFoundMsg, "%s");

		return this;
	}

	public String getCommandErrorMsg() {
		return commandErrorMsg;
	}

	/**
	 * <p>Sets the displayed message when an exception occurs in a command</p>
	 * <p><i>Default message : {@value #DEFAULT_COMMAND_ERROR_MESSAGE}</i></p>
	 *
	 * @param commandErrorMsg Message to display when an exception occurs in a command
	 * @return This object for chaining convenience
	 */
	public DefaultMessages setCommandErrorMsg(String commandErrorMsg) {
		this.commandErrorMsg = Utils.requireNonBlank(commandErrorMsg, "Command error message");

		return this;
	}

	public String getApplicationCommandNotFoundMsg() {
		return applicationCommandNotFoundMsg;
	}

	/**
	 * <p>Sets the displayed message when a application command is not found, which should be impossible in theory</p>
	 * <p><i>Default message : {@value #DEFAULT_APPLICATION_COMMAND_NOT_FOUND_MESSAGE}</i></p>
	 *
	 * @param applicationCommandNotFoundMsg Message to display when a application command is not found
	 * @return This object for chaining convenience
	 */
	public DefaultMessages setApplicationCommandNotFoundMsg(String applicationCommandNotFoundMsg) {
		this.applicationCommandNotFoundMsg = Utils.requireNonBlank(applicationCommandNotFoundMsg, "'Slash command not found' error message");

		return this;
	}

	public String getApplicationCommandErrorMsg() {
		return applicationCommandErrorMsg;
	}

	/**
	 * <p>Sets the displayed message when an exception occurs in a application command</p>
	 * <p><i>Default message : {@value #DEFAULT_APPLICATION_COMMAND_ERROR_MESSAGE}</i></p>
	 *
	 * @param applicationCommandErrorMsg Message to display when an exception occurs in a application command
	 * @return This object for chaining convenience
	 */
	public DefaultMessages setApplicationCommandErrorMsg(String applicationCommandErrorMsg) {
		this.applicationCommandErrorMsg = Utils.requireNonBlank(applicationCommandErrorMsg, "Slash command error message");

		return this;
	}

	public String getSlashCommandUnresolvableParameterMsg() {
		return slashCommandUnresolvableParameterMsg;
	}

	/**
	 * <p>Sets the displayed message when a application command parameter is unresolvable</p>
	 * <b>The message must have <b>two</b> %s format specifier to insert the unresolved parameter</b>
	 * <p><i>Default message : {@value #DEFAULT_SLASH_COMMAND_UNRESOLVABLE_PARAMETER_MESSAGE}</i></p>
	 *
	 * @param slashCommandUnresolvableParameterMsg Message to display when a application command parameter is unresolvable
	 * @return This object for chaining convenience
	 */
	public DefaultMessages setSlashCommandUnresolvableParameterMsg(String slashCommandUnresolvableParameterMsg) {
		Utils.requireNonBlank(slashCommandUnresolvableParameterMsg, "Slash command unresolvable parameter error message");
		this.slashCommandUnresolvableParameterMsg = checkFormatters(slashCommandUnresolvableParameterMsg, "%s", "%s");

		return this;
	}

	public String getSlashCommandInvalidParameterTypeMsg() {
		return slashCommandInvalidParameterTypeMsg;
	}

	/**
	 * <p>Sets the displayed message when a application command parameter is resolved into an invalid type</p>
	 * <b>The message must have <b>three</b> %s format specifier to insert the needed parameter types </b>
	 * <p><i>Default message : {@value #DEFAULT_SLASH_COMMAND_INVALID_PARAMETER_TYPE_MESSAGE}</i></p>
	 *
	 * @param slashCommandInvalidParameterTypeMsg Message to display when a application command parameter is resolved into an invalid type
	 * @return This object for chaining convenience
	 */
	public DefaultMessages setSlashCommandInvalidParameterTypeMsg(String slashCommandInvalidParameterTypeMsg) {
		Utils.requireNonBlank(slashCommandInvalidParameterTypeMsg, "Slash command invalid parameter type error message");
		this.slashCommandInvalidParameterTypeMsg = checkFormatters(slashCommandInvalidParameterTypeMsg, "%s", "%s", "%s");

		return this;
	}

	public String getNullComponentTypeErrorMsg() {
		return nullComponentTypeErrorMsg;
	}

	/**
	 * <p>Sets the displayed message when a component type is null (so the component wasn't found)</p>
	 * <p><i>Default message : {@value #DEFAULT_NULL_COMPONENT_TYPE_ERROR_MESSAGE}</i></p>
	 *
	 * @param nullComponentTypeErrorMsg Message to display when a component type is null
	 * @return This object for chaining convenience
	 */
	public DefaultMessages setNullComponentTypeErrorMsg(String nullComponentTypeErrorMsg) {
		this.nullComponentTypeErrorMsg = Utils.requireNonBlank(nullComponentTypeErrorMsg, "Null component id error message");

		return this;
	}

	public String getClosedDMErrorMsg() {
		return this.closedDMErrorMsg;
	}

	/**
	 * <p>Sets the displayed message when a User's DMs are closed (when sending help content for example)</p>
	 * <p><i>Default message : {@value #DEFAULT_CLOSED_DM_ERROR_MESSAGE}</i></p>
	 *
	 * @param closedDMErrorMsg Message to display when a User's DMs are closed
	 * @return This object for chaining convenience
	 */
	public DefaultMessages setClosedDMErrorMsg(String closedDMErrorMsg) {
		this.closedDMErrorMsg = Utils.requireNonBlank(closedDMErrorMsg, "Closed DMs error message");
		
		return this;
	}
}
