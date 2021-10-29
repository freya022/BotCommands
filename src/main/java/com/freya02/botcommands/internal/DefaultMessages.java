package com.freya02.botcommands.internal;

import com.freya02.botcommands.api.SettingsProvider;
import com.freya02.botcommands.api.annotations.NSFW;
import com.freya02.botcommands.internal.utils.BResourceBundle;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.StringJoiner;

/**
 * Class which holds all the strings the framework may use
 * <br>The strings are formatted as follows:
 * <br><code>Default label name = default value</code>
 * <br>The label name may be used in another DefaultMessages.properties file in order to localize these strings
 *
 * <ul>
 *     <li>userPermErrorMsg = {@value #DEFAULT_USER_PERM_ERROR_MESSAGE}</li>
 *     <li>botPermErrorMsg = {@value DEFAULT_BOT_PERM_ERROR_MESSAGE}</li>
 *     <li>ownerOnlyErrorMsg = {@value DEFAULT_OWNER_ONLY_ERROR_MESSAGE}</li>
 * </ul>
 * <ul>
 *     <li>userCooldownMsg = {@value DEFAULT_USER_COOLDOWN_MESSAGE}</li>
 *     <li>channelCooldownMsg = {@value DEFAULT_CHANNEL_COOLDOWN_MESSAGE}</li>
 *     <li>guildCooldownMsg = {@value DEFAULT_GUILD_COOLDOWN_MESSAGE}</li>
 * </ul>
 * <ul>
 *     <li>commandNotFoundMsg = {@value DEFAULT_COMMAND_NOT_FOUND_MESSAGE}</li>
 *     <li>commandErrorMsg = {@value DEFAULT_COMMAND_ERROR_MESSAGE}</li>
 *     <li>closedDMErrorMsg = {@value DEFAULT_CLOSED_DM_ERROR_MESSAGE}</li>
 *     <li>applicationCommandNotFoundMsg = {@value DEFAULT_APPLICATION_COMMAND_NOT_FOUND_MESSAGE}</li>
 *     <li>applicationCommandErrorMsg = {@value DEFAULT_APPLICATION_COMMAND_ERROR_MESSAGE}</li>
 *     <li>componentHandlerErrorMsg = {@value DEFAULT_COMPONENT_HANDLER_ERROR_MESSAGE}</li>
 *     <li>componentCallbackErrorMsg = {@value DEFAULT_COMPONENT_CALLBACK_ERROR_MESSAGE}</li>
 *     <li>slashCommandUnresolvableParameterMsg = {@value DEFAULT_SLASH_COMMAND_UNRESOLVABLE_PARAMETER_MESSAGE}</li>
 *     <li>slashCommandInvalidParameterTypeMsg = {@value DEFAULT_SLASH_COMMAND_INVALID_PARAMETER_TYPE_MESSAGE}</li>
 *     <li>nullComponentTypeErrorMsg = {@value DEFAULT_NULL_COMPONENT_TYPE_ERROR_MESSAGE}</li>
 * </ul>
 * <ul>
 *     <li>nsfwDisabledErrorMsg = {@value DEFAULT_NSFW_DISABLED_ERROR_MESSAGE}</li>
 *     <li>nsfwOnlyErrorMsg = {@value DEFAULT_NSFW_ONLY_ERROR_MESSAGE}</li>
 *     <li>nsfwDMDeniedErrorMsg = {@value DEFAULT_NSFW_DM_DENIED_ERROR_MESSAGE}</li>
 * </ul>
 *
 * @see SettingsProvider#getLocale(Guild)
 * @see SettingsProvider#doesUserConsentNSFW(User)
 */
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
	public static final String DEFAULT_COMPONENT_HANDLER_ERROR_MESSAGE = "An uncaught exception occurred";
	public static final String DEFAULT_COMPONENT_CALLBACK_ERROR_MESSAGE = "An uncaught exception occurred";
	public static final String DEFAULT_SLASH_COMMAND_UNRESOLVABLE_PARAMETER_MESSAGE = "The parameter '%s' could not be resolved into a %s";
	public static final String DEFAULT_SLASH_COMMAND_INVALID_PARAMETER_TYPE_MESSAGE = "The parameter '%s' is not a valid type (expected a %s, got a %s)";
	public static final String DEFAULT_NULL_COMPONENT_TYPE_ERROR_MESSAGE = "This component is not usable anymore";
	public static final String DEFAULT_NSFW_DISABLED_ERROR_MESSAGE = "This NSFW command is disabled in this kind of channel";
	public static final String DEFAULT_NSFW_ONLY_ERROR_MESSAGE = "This command can only be used in NSFW channels";
	public static final String DEFAULT_NSFW_DM_DENIED_ERROR_MESSAGE = "This command cannot be used in DMs unless you consent";

	private final String userPermErrorMsg;
	private final String botPermErrorMsg;
	private final String ownerOnlyErrorMsg;

	private final String userCooldownMsg;
	private final String channelCooldownMsg;
	private final String guildCooldownMsg;

	private final String commandNotFoundMsg;
	private final String commandErrorMsg;
	private final String closedDMErrorMsg;
	private final String applicationCommandNotFoundMsg;
	private final String applicationCommandErrorMsg;
	private final String componentHandlerErrorMsg;
	private final String componentCallbackErrorMsg;
	private final String slashCommandUnresolvableParameterMsg;
	private final String slashCommandInvalidParameterTypeMsg;
	private final String nullComponentTypeErrorMsg;

	private final String nsfwDisabledErrorMsg;
	private final String nsfwOnlyErrorMsg;
	private final String nsfwDMDeniedErrorMsg;

	public DefaultMessages(@NotNull Locale locale) {
		final BResourceBundle bundle = BResourceBundle.getBundle("DefaultMessages", locale);

		userPermErrorMsg = getValue(bundle, "userPermErrorMsg", DEFAULT_USER_PERM_ERROR_MESSAGE);
		botPermErrorMsg = checkFormatters(getValue(bundle, "botPermErrorMsg", DEFAULT_BOT_PERM_ERROR_MESSAGE), "%s");
		ownerOnlyErrorMsg = getValue(bundle, "ownerOnlyErrorMsg", DEFAULT_OWNER_ONLY_ERROR_MESSAGE);

		userCooldownMsg = checkFormatters(getValue(bundle, "userCooldownMsg", DEFAULT_USER_COOLDOWN_MESSAGE), "%.2f");
		channelCooldownMsg = checkFormatters(getValue(bundle, "channelCooldownMsg", DEFAULT_CHANNEL_COOLDOWN_MESSAGE), "%.2f");
		guildCooldownMsg = checkFormatters(getValue(bundle, "guildCooldownMsg", DEFAULT_GUILD_COOLDOWN_MESSAGE), "%.2f");

		commandNotFoundMsg = checkFormatters(getValue(bundle, "commandNotFoundMsg", DEFAULT_COMMAND_NOT_FOUND_MESSAGE), "%s");
		commandErrorMsg = getValue(bundle, "commandErrorMsg", DEFAULT_COMMAND_ERROR_MESSAGE);
		closedDMErrorMsg = getValue(bundle, "closedDMErrorMsg", DEFAULT_CLOSED_DM_ERROR_MESSAGE);
		applicationCommandNotFoundMsg = getValue(bundle, "applicationCommandNotFoundMsg", DEFAULT_APPLICATION_COMMAND_NOT_FOUND_MESSAGE);
		applicationCommandErrorMsg = getValue(bundle, "applicationCommandErrorMsg", DEFAULT_APPLICATION_COMMAND_ERROR_MESSAGE);
		componentHandlerErrorMsg = getValue(bundle, "componentHandlerErrorMsg", DEFAULT_COMPONENT_HANDLER_ERROR_MESSAGE);
		componentCallbackErrorMsg = getValue(bundle, "componentCallbackErrorMsg", DEFAULT_COMPONENT_CALLBACK_ERROR_MESSAGE);
		slashCommandUnresolvableParameterMsg = checkFormatters(getValue(bundle, "slashCommandUnresolvableParameterMsg", DEFAULT_SLASH_COMMAND_UNRESOLVABLE_PARAMETER_MESSAGE), "%s", "%s");
		slashCommandInvalidParameterTypeMsg = checkFormatters(getValue(bundle, "slashCommandInvalidParameterTypeMsg", DEFAULT_SLASH_COMMAND_INVALID_PARAMETER_TYPE_MESSAGE), "%s", "%s", "%s");
		nullComponentTypeErrorMsg = getValue(bundle, "nullComponentTypeErrorMsg", DEFAULT_NULL_COMPONENT_TYPE_ERROR_MESSAGE);

		nsfwDisabledErrorMsg = getValue(bundle, "nsfwDisabledErrorMsg", DEFAULT_NSFW_DISABLED_ERROR_MESSAGE);
		nsfwOnlyErrorMsg = getValue(bundle, "nsfwOnlyErrorMsg", DEFAULT_NSFW_ONLY_ERROR_MESSAGE);
		nsfwDMDeniedErrorMsg = getValue(bundle, "nsfwDMDeniedErrorMsg", DEFAULT_NSFW_DM_DENIED_ERROR_MESSAGE);
	}

	@NotNull
	private static String getValue(@Nullable BResourceBundle bundle, @NotNull String label, @NotNull String defaultVal) {
		if (bundle == null) return defaultVal;

		return bundle.getValueOrDefault(label, defaultVal);
	}

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

	/**
	 * @return Message to display when the user does not have enough permissions
	 */
	public String getUserPermErrorMsg() {
		return userPermErrorMsg;
	}

	/**
	 * <b>The message must have a %s format specifier to insert the needed permissions</b>
	 *
	 * @return Message to display when the bot does not have enough permissions
	 */
	public String getBotPermErrorMsg() {
		return botPermErrorMsg;
	}

	/**
	 * @return Message to display when the command is only usable by the owner
	 */
	public String getOwnerOnlyErrorMsg() {
		return ownerOnlyErrorMsg;
	}

	/**
	 * <p><b>Requires one string format for the per-user cooldown time (in seconds)</b></p>
	 *
	 * @return Message to display when the command is on per-user cooldown
	 */
	public String getUserCooldownMsg() {
		return userCooldownMsg;
	}

	/**
	 * <p><b>Requires one string format for the per-channel cooldown time (in seconds)</b></p>
	 *
	 * @return Message to display when the command is on per-channel cooldown
	 */
	public String getChannelCooldownMsg() {
		return channelCooldownMsg;
	}

	/**
	 * <p><b>Requires one string format for the per-guild cooldown time (in seconds)</b></p>
	 *
	 * @return Message to display when the command is on per-guild cooldown
	 */
	public String getGuildCooldownMsg() {
		return guildCooldownMsg;
	}

	/**
	 * <b>The message must have a %s format specifier to insert the suggested commands</b>
	 *
	 * @return Message to display when the command is not found
	 */
	public String getCommandNotFoundMsg() {
		return commandNotFoundMsg;
	}

	/**
	 * @return Message to display when an exception occurs in a command
	 */
	public String getCommandErrorMsg() {
		return commandErrorMsg;
	}

	/**
	 * @return Message to display when a application command is not found
	 */
	public String getApplicationCommandNotFoundMsg() {
		return applicationCommandNotFoundMsg;
	}

	/**
	 * @return Message to display when an exception occurs in a application command
	 */
	public String getApplicationCommandErrorMsg() {
		return applicationCommandErrorMsg;
	}

	/**
	 * @return Message to display when an exception occurs in a component ID handler
	 */
	public String getComponentHandlerErrorMsg() {
		return componentHandlerErrorMsg;
	}

	/**
	 * @return Message to display when an exception occurs in a component callback
	 */
	public String getComponentCallbackErrorMsg() {
		return componentCallbackErrorMsg;
	}

	/**
	 * <b>The message must have <b>two</b> %s format specifier to insert the unresolved parameter</b>
	 *
	 * @return Message to display when a application command parameter is unresolvable
	 */
	public String getSlashCommandUnresolvableParameterMsg() {
		return slashCommandUnresolvableParameterMsg;
	}

	/**
	 * <b>The message must have <b>three</b> %s format specifier to insert the needed parameter types </b>
	 *
	 * @return Message to display when a application command parameter is resolved into an invalid type
	 */
	public String getSlashCommandInvalidParameterTypeMsg() {
		return slashCommandInvalidParameterTypeMsg;
	}

	/**
	 * @return Message to display when a component type is null (The ID is unresolvable / not found)
	 */
	public String getNullComponentTypeErrorMsg() {
		return nullComponentTypeErrorMsg;
	}

	/**
	 * @return Message to display when a User's DMs are closed (when sending help content for example)
	 */
	public String getClosedDMErrorMsg() {
		return this.closedDMErrorMsg;
	}

	/**
	 * @return Message to display when a command is used in a channel type that was not enabled by {@link NSFW @NSFW}
	 */
	public String getNsfwDisabledErrorMsg() {
		return nsfwDisabledErrorMsg;
	}

	/**
	 * @return Message to display when a command is used in a non-NSFW {@link TextChannel}
	 */
	public String getNSFWOnlyErrorMsg() {
		return this.nsfwOnlyErrorMsg;
	}

	/**
	 * @return Message to display when a command is used in DMs and the user has not given consent yet
	 */
	public String getNSFWDMDeniedErrorMsg() {
		return this.nsfwDMDeniedErrorMsg;
	}
}
