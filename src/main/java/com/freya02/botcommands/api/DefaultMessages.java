package com.freya02.botcommands.api;

import com.freya02.botcommands.api.annotations.NSFW;
import com.freya02.botcommands.api.localization.Localization;
import com.freya02.botcommands.api.localization.LocalizationTemplate;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

import static com.freya02.botcommands.api.localization.Localization.Entry.entry;

/**
 * Class which holds all the strings the framework may use
 * <p>The default values are contained in the resources, at <code>bc_localization/DefaultMessages.json</code>
 * <p>You may change the default values by:
 * <ul>
 *     <li>Creating a new <code>DefaultMessages.json</code> in the resource folder <code>bc_localization</code>, effectively overriding the default resource</li>
 *     <li>Creating language variations with a file with the same name, suffixed by the locale string, in the same folder, for example <code>bc_localization/DefaultMessages_fr.json</code></li>
 * </ul>
 * <p>The resulting paths must not be changed, however the localization templates can have their values in any order, and different format specifiers, but need to keep the same names
 *
 * <p>Refer to {@link Localization} for more details
 *
 * @see SettingsProvider#doesUserConsentNSFW(User)
 * @see Localization
 */
public final class DefaultMessages {
	@NotNull private final Localization defaultLocalization;
	@Nullable private final Localization localization;

	/**
	 * <b>THIS IS NOT A PUBLIC CONSTRUCTOR</b>
	 */
	@ApiStatus.Internal
	public DefaultMessages(@NotNull Locale locale) {
		final Localization defaultLocalization = Localization.getInstance("DefaultMessages_default", locale);
		if (defaultLocalization == null) {
			throw new IllegalStateException("Could not find any DefaultMessages_default bundle");
		}

		this.defaultLocalization = defaultLocalization;
		this.localization = Localization.getInstance("DefaultMessages", locale);
	}

	@NotNull
	private LocalizationTemplate getLocalizationTemplate(@NotNull String path) {
		LocalizationTemplate template = localization == null
				? null
				: localization.get(path);

		if (template == null) {
			template = defaultLocalization.get(path);
		}

		if (template == null) {
			throw new IllegalArgumentException("Localization template for default messages '" + path + "' could not be found");
		}

		return template;
	}

	/**
	 * @return Message to display when the user does not have enough permissions
	 */
	public String getUserPermErrorMsg() {
		return getLocalizationTemplate("user.perm.error.message").localize();
	}

	/**
	 * @return Message to display when the bot does not have enough permissions
	 */
	public String getBotPermErrorMsg(String permissionsString) {
		return getLocalizationTemplate("bot.perm.error.message").localize(entry("permissions", permissionsString));
	}

	/**
	 * @return Message to display when the command is only usable by the owner
	 */
	public String getOwnerOnlyErrorMsg() {
		return getLocalizationTemplate("owner.only.error.message").localize();
	}

	/**
	 * @return Message to display when the command is on per-user cooldown
	 */
	public String getUserCooldownMsg(double cooldown) {
		return getLocalizationTemplate("user.cooldown.message").localize(entry("cooldown", cooldown));
	}

	/**
	 * @return Message to display when the command is on per-channel cooldown
	 */
	public String getChannelCooldownMsg(double cooldown) {
		return getLocalizationTemplate("channel.cooldown.message").localize(entry("cooldown", cooldown));
	}

	/**
	 * @return Message to display when the command is on per-guild cooldown
	 */
	public String getGuildCooldownMsg(double cooldown) {
		return getLocalizationTemplate("guild.cooldown.message").localize(entry("cooldown", cooldown));
	}

	/**
	 * @return Message to display when the command is not found
	 */
	public String getCommandNotFoundMsg(String suggestions) {
		return getLocalizationTemplate("command.not.found.message").localize(entry("suggestions", suggestions));
	}

	/**
	 * @return Message to display when an exception occurs in a command
	 */
	public String getCommandErrorMsg() {
		return getLocalizationTemplate("command.error.message").localize();
	}

	/**
	 * @return Message to display when an application command is not found
	 */
	public String getApplicationCommandNotFoundMsg() {
		return getLocalizationTemplate("application.command.not.found.message").localize();
	}

	/**
	 * @return Message to display when an exception occurs in an application command
	 */
	public String getApplicationCommandErrorMsg() {
		return getLocalizationTemplate("application.command.error.message").localize();
	}

	/**
	 * @return Message to display when an exception occurs in a component ID handler
	 */
	public String getComponentHandlerErrorMsg() {
		return getLocalizationTemplate("component.handler.error.message").localize();
	}

	/**
	 * @return Message to display when an exception occurs in a component callback
	 */
	public String getComponentCallbackErrorMsg() {
		return getLocalizationTemplate("component.callback.error.message").localize();
	}

	/**
	 * @return Message to display when an application command parameter is unresolvable
	 */
	public String getSlashCommandUnresolvableParameterMsg(String parameterName, String parameterType) {
		return getLocalizationTemplate("slash.command.unresolvable.parameter.message").localize(
				entry(parameterName, parameterName),
				entry(parameterType, parameterType)
		);
	}

	/**
	 * @return Message to display when an application command parameter is resolved into an invalid type
	 */
	public String getSlashCommandInvalidParameterTypeMsg(String parameterName, String expectedType, String actualType) {
		return getLocalizationTemplate("slash.command.invalid.parameter.type.message").localize(
				entry("parameterName", parameterName),
				entry("expectedType", expectedType),
				entry("actualType", actualType)
		);
	}

	/**
	 * @return Message to display when a component type is null (The ID is unresolvable / not found)
	 */
	public String getNullComponentTypeErrorMsg() {
		return getLocalizationTemplate("null.component.type.error.message").localize();
	}

	/**
	 * @return Message to display when a User's DMs are closed (when sending help content for example)
	 */
	public String getClosedDMErrorMsg() {
		return getLocalizationTemplate("closed.dm.error.message").localize();
	}

	/**
	 * @return Message to display when a command is used in a channel type that was not enabled by {@link NSFW @NSFW}
	 */
	public String getNsfwDisabledErrorMsg() {
		return getLocalizationTemplate("nsfw.disabled.error.message").localize();
	}

	/**
	 * @return Message to display when a command is used in a non-NSFW {@link GuildMessageChannel}
	 */
	public String getNSFWOnlyErrorMsg() {
		return getLocalizationTemplate("nsfw.only.error.message").localize();
	}

	/**
	 * @return Message to display when a command is used in DMs and the user has not given consent yet
	 */
	public String getNSFWDMDeniedErrorMsg() {
		return getLocalizationTemplate("nsfw.dm.denied.error.message").localize();
	}

	/**
	 * @return Message to display when a user tries to use a component it cannot interact with
	 */
	public String getComponentNotAllowedErrorMsg() {
		return getLocalizationTemplate("component.not.allowed.error.message").localize();
	}

	/**
	 * @return Message to display when a user tries to use a component which has reached timeout while the bot was offline
	 */
	public String getComponentExpiredErrorMsg() {
		return getLocalizationTemplate("component.expired.error.message").localize();
	}

	/**
	 * @return Message to display when a user tries to use a component which is not know by this library
	 */
	public String getComponentNotFoundErrorMsg() {
		return getLocalizationTemplate("component.not.found.error.message").localize();
	}

	/**
	 * @return Message to display when a user tries to use a component which has invalid data
	 * <br>This is usually when the number of arguments don't match up, so the button listener has a different signature
	 */
	public String getComponentInvalidDataErrorMsg() {
		return getLocalizationTemplate("component.invalid.data.error.message").localize();
	}
}
