package com.freya02.botcommands;

public final class DefaultMessages {
	private String userPermErrorMsg = "You are not allowed to do this";
	private String botPermErrorMsg = "I don't have the required permissions to do this";
	private String ownerOnlyErrorMsg = "Only the owner can use this";

	private String userCooldownMsg = "You must wait **%.2f seconds**";
	private String channelCooldownMsg = "You must wait **%.2f seconds in this channel**";
	private String guildCooldownMsg = "You must wait **%.2f seconds in this guild**";

	private String commandNotFoundMsg = "Unknown command, maybe you meant: %s";

	void setUserPermErrorMsg(String userPermErrorMsg) {
		this.userPermErrorMsg = Utils.requireNonBlankString(userPermErrorMsg, "User permission error message is null");
	}

	void setBotPermErrorMsg(String botPermErrorMsg) {
		this.botPermErrorMsg = Utils.requireNonBlankString(botPermErrorMsg, "Bot permission error message is null");
	}

	void setOwnerOnlyErrorMsg(String ownerOnlyErrorMsg) {
		this.ownerOnlyErrorMsg = Utils.requireNonBlankString(ownerOnlyErrorMsg, "Owner only error message is null");
	}

	void setUserCooldownMsg(String userCooldownMsg) {
		Utils.requireNonBlankString(userCooldownMsg, "User cooldown error message is null");
		if (!userCooldownMsg.contains("%.2f"))
			throw new IllegalArgumentException("User cooldown message should contain one '%.2f' format specifier");
		this.userCooldownMsg = userCooldownMsg;
	}

	void setChannelCooldownMsg(String channelCooldownMsg) {
		Utils.requireNonBlankString(channelCooldownMsg, "Channel cooldown error message is null");
		if (!channelCooldownMsg.contains("%.2f"))
			throw new IllegalArgumentException("Channel cooldown message should contain one '%.2f' format specifier");
		this.channelCooldownMsg = channelCooldownMsg;
	}

	void setGuildCooldownMsg(String guildCooldownMsg) {
		Utils.requireNonBlankString(guildCooldownMsg, "Guild cooldown error message is null");
		if (!guildCooldownMsg.contains("%.2f"))
			throw new IllegalArgumentException("Guild cooldown message should contain one '%.2f' format specifier");
		this.guildCooldownMsg = guildCooldownMsg;
	}

	void setCommandNotFoundMsg(String commandNotFoundMsg) {
		Utils.requireNonBlankString(commandNotFoundMsg, "'Command not found' error message is null");
		if (!commandNotFoundMsg.contains("%s")) {
			throw new IllegalArgumentException("The 'Command not found' string must contain one %s formatter");
		}
		this.commandNotFoundMsg = commandNotFoundMsg;
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
