package com.freya02.botcommands.api;

import com.freya02.botcommands.api.commands.prefixed.annotations.NSFW;
import com.freya02.botcommands.api.core.SettingsProvider;
import com.freya02.botcommands.api.localization.Localization;
import com.freya02.botcommands.api.localization.LocalizationTemplate;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static com.freya02.botcommands.api.localization.Localization.Entry.entry;

/**
 * Class which holds all the strings the framework may use
 * <p>The default values are contained in the resources, at {@code bc_localization/DefaultMessages.json}
 * <p>You may change the default values by:
 * <ul>
 *     <li>Creating a new {@code DefaultMessages.json} in the resource folder {@code bc_localization}, effectively overriding the default resource</li>
 *     <li>Creating language variations with a file with the same name, suffixed by the locale string, in the same folder, for example {@code bc_localization/DefaultMessages_fr.json}</li>
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
		final LocalizationTemplate template = getLocalizationTemplateOrNull(path);
		if (template == null) {
			throw new IllegalArgumentException("Localization template for default messages '" + path + "' could not be found");
		}

		return template;
	}

	@Nullable
	private LocalizationTemplate getLocalizationTemplateOrNull(@NotNull String path) {
		LocalizationTemplate template = localization == null
				? null
				: localization.get(path);

		if (template == null) {
			template = defaultLocalization.get(path);
		}

		return template;
	}

	/**
	 * @return The localized permission, or {@link Permission#getName()} if the translation is missing.
	 */
	@NotNull
	public String getPermission(Permission permission) {
		final LocalizationTemplate localizationTemplate = getLocalizationTemplateOrNull("permissions." + permission.name());
		return localizationTemplate != null ? localizationTemplate.localize() : permission.getName();
	}

	/**
	 * @return Message to display when an uncaught exception occurs
	 */
	public String getGeneralErrorMsg() {
		return getLocalizationTemplate("general_error_message").localize();
	}

	/**
	 * @return Message to display when the user does not have enough permissions
	 */
	public String getUserPermErrorMsg(Set<Permission> permissions) {
		final String localizedPermissions = permissions.stream().map(this::getPermission).collect(Collectors.joining(", "));
		return getLocalizationTemplate("user.perm.error.message").localize(entry("permissions", localizedPermissions));
	}

	/**
	 * @return Message to display when the bot does not have enough permissions
	 */
	public String getBotPermErrorMsg(Set<Permission> permissions) {
		final String localizedPermissions = permissions.stream().map(this::getPermission).collect(Collectors.joining(", "));
		return getLocalizationTemplate("bot.perm.error.message").localize(entry("permissions", localizedPermissions));
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
	 * @return Message to display when a slash command option is unresolvable (only in slash command interactions)
	 */
	public String getSlashCommandUnresolvableOptionMsg(String parameterName) {
		return getLocalizationTemplate("slash.command.unresolvable.option.message").localize(
				entry("optionName", parameterName)
		);
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
	 * @return Message to display when a user tries to use a modal which has reached timeout
	 */
	public String getModalExpiredErrorMsg() {
		return getLocalizationTemplate("modal.expired.error.message").localize();
	}
}
