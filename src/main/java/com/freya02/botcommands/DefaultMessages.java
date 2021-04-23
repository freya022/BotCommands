package com.freya02.botcommands;

public final class DefaultMessages {
	private String userPermErrorMsg = "You are not allowed to do this";
	private String botPermErrorMsg = "I am missing these permissions: %s";
	private String ownerOnlyErrorMsg = "Only the owner can use this";

	private String userCooldownMsg = "You must wait **%.2f seconds**";
	private String channelCooldownMsg = "You must wait **%.2f seconds in this channel**";
	private String guildCooldownMsg = "You must wait **%.2f seconds in this guild**";

	private String commandNotFoundMsg = "Unknown command, maybe you meant: %s";

	/** <p>Sets the displayed message when the user does not have enough permissions</p>
	 * <p><i>Default message : You are not allowed to do this</i></p>
	 * @param userPermErrorMsg Message to display when the user does not have enough permissions
	 * @return This object for chaining convenience
	 */
	public DefaultMessages setUserPermErrorMsg(String userPermErrorMsg) {
		this.userPermErrorMsg = Utils.requireNonBlankString(userPermErrorMsg, "User permission error message is null");

		return this;
	}

	/** <p>Sets the displayed message when the bot does not have enough permissions</p>
	 * <p><i>Default message : I am missing these permissions: %s</i></p>
	 * <b>The message must have a %s format specifier to insert the needed permissions</b>
	 * @param botPermErrorMsg Message to display when the bot does not have enough permissions
	 * @return This object for chaining convenience
	 */
	public DefaultMessages setBotPermErrorMsg(String botPermErrorMsg) {
		Utils.requireNonBlankString(botPermErrorMsg, "Bot permission error message is null");
		if (!botPermErrorMsg.contains("%s")) {
			throw new IllegalArgumentException("The bot permission error string must contain one %s formatter");
		}
		this.botPermErrorMsg = botPermErrorMsg;

		return this;
	}

	/** <p>Sets the displayed message when the command is only usable by the owner</p>
	 * <p><i>Default message : Only the owner can use this</i></p>
	 * @param ownerOnlyErrorMsg Message to display when the command is only usable by the owner
	 * @return This object for chaining convenience
	 */
	public DefaultMessages setOwnerOnlyErrorMsg(String ownerOnlyErrorMsg) {
		this.ownerOnlyErrorMsg = Utils.requireNonBlankString(ownerOnlyErrorMsg, "Owner only error message is null");

		return this;
	}

	/** <p>Sets the displayed message when the command is on per-user cooldown</p>
	 * <p><b>Requires one string format for the per-user cooldown time (in seconds)</b></p>
	 * <p><i>Default message : You must wait **%.2f seconds**</i></p>
	 * @param userCooldownMsg Message to display when the command is on per-user cooldown
	 * @return This object for chaining convenience
	 */
	public DefaultMessages setUserCooldownMsg(String userCooldownMsg) {
		Utils.requireNonBlankString(userCooldownMsg, "User cooldown error message is null");
		if (!userCooldownMsg.contains("%.2f"))
			throw new IllegalArgumentException("User cooldown message should contain one '%.2f' format specifier");
		this.userCooldownMsg = userCooldownMsg;

		return this;
	}

	/** <p>Sets the displayed message when the command is on per-channel cooldown</p>
	 * <p><b>Requires one string format for the per-channel cooldown time (in seconds)</b></p>
	 * <p><i>Default message : You must wait **%.2f seconds in this channel**</i></p>
	 * @param channelCooldownMsg Message to display when the command is on per-channel cooldown
	 * @return This object for chaining convenience
	 */
	public DefaultMessages setChannelCooldownMsg(String channelCooldownMsg) {
		Utils.requireNonBlankString(channelCooldownMsg, "Channel cooldown error message is null");
		if (!channelCooldownMsg.contains("%.2f"))
			throw new IllegalArgumentException("Channel cooldown message should contain one '%.2f' format specifier");
		this.channelCooldownMsg = channelCooldownMsg;

		return this;
	}

	/** <p>Sets the displayed message when the command is on per-guild cooldown</p>
	 * <p><b>Requires one string format for the per-guild cooldown time (in seconds)</b></p>
	 * <p><i>Default message : You must wait **%.2f seconds in this guild**</i></p>
	 * @param guildCooldownMsg Message to display when the command is on per-guild cooldown
	 * @return This object for chaining convenience
	 */
	public DefaultMessages setGuildCooldownMsg(String guildCooldownMsg) {
		Utils.requireNonBlankString(guildCooldownMsg, "Guild cooldown error message is null");
		if (!guildCooldownMsg.contains("%.2f"))
			throw new IllegalArgumentException("Guild cooldown message should contain one '%.2f' format specifier");
		this.guildCooldownMsg = guildCooldownMsg;

		return this;
	}

	/** <p>Sets the displayed message when the command is not found</p>
	 * <p><i>Default message : Unknown command, maybe you meant: %s</i></p>
	 * <b>The message must have a %s format specifier to insert the suggested commands</b>
	 * @param commandNotFoundMsg Message to display when the command is not found
	 * @return This object for chaining convenience
	 */
	public DefaultMessages setCommandNotFoundMsg(String commandNotFoundMsg) {
		Utils.requireNonBlankString(commandNotFoundMsg, "'Command not found' error message is null");
		if (!commandNotFoundMsg.contains("%s")) {
			throw new IllegalArgumentException("The 'Command not found' string must contain one %s formatter");
		}
		this.commandNotFoundMsg = commandNotFoundMsg;

		return this;
	}

	public String getUserPermErrorMsg() {
		return userPermErrorMsg;
	}

	public String getBotPermErrorMsg() {
		return botPermErrorMsg;
	}

	public String getOwnerOnlyErrorMsg() {
		return ownerOnlyErrorMsg;
	}

	public String getUserCooldownMsg() {
		return userCooldownMsg;
	}

	public String getChannelCooldownMsg() {
		return channelCooldownMsg;
	}

	public String getGuildCooldownMsg() {
		return guildCooldownMsg;
	}

	public String getCommandNotFoundMsg() {
		return commandNotFoundMsg;
	}
}
