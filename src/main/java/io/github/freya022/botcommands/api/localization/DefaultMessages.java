package io.github.freya022.botcommands.api.localization;

import io.github.freya022.botcommands.api.commands.text.annotations.NSFW;
import io.github.freya022.botcommands.api.core.BContext;
import io.github.freya022.botcommands.api.core.SettingsProvider;
import io.github.freya022.botcommands.api.localization.providers.LocalizationMapProvider;
import io.github.freya022.botcommands.api.localization.readers.LocalizationMapReader;
import io.github.freya022.botcommands.internal.utils.ExceptionsKt;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.utils.Timestamp;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.freya022.botcommands.api.localization.Localization.Entry.entry;

/**
 * Helper class to translate framework-specific messages.
 *
 * <p>The default values are contained in the {@code resources}, at {@code /bc_localization/DefaultMessages-default.json}.
 *
 * <p>You may change the default values by:
 * <ul>
 *     <li>Creating a new {@code DefaultMessages.json}</li>
 *     <li>Creating language variations with a file of the same name, suffixed by the {@link Locale#toLanguageTag() locale tag},
 *         for example {@code /bc_localization/DefaultMessages_fr.json}</li>
 * </ul>
 *
 * <p>You can always customize:
 * <ul>
 *     <li>Loading directories: by creating new {@link LocalizationMapProvider LocalizationMap providers}</li>
 *     <li>File formats: by creating new {@link LocalizationMapReader LocalizationMap readers}</li>
 *     <li>Template formats: by creating new {@link LocalizationTemplate localization templates}</li>
 * </ul>
 * Most of the time it's easier to just add your new localization files in {@code /bc_localization}.
 *
 * <p>The localization paths must not be changed, of course, the templates can have their values in any order,
 * use different format specifiers, but need to keep the same names.
 *
 * <p>Refer to {@link Localization} for more details.
 *
 * @see SettingsProvider#doesUserConsentNSFW(User)
 * @see Localization
 */
public final class DefaultMessages {
	private final Localization localization;

	/**
	 * <b>THIS IS NOT A PUBLIC CONSTRUCTOR</b>
	 */
	@ApiStatus.Internal
	public DefaultMessages(@NotNull BContext context, @NotNull Locale locale) {
		final LocalizationService localizationService = context.getService(LocalizationService.class);

		this.localization = localizationService.getInstance("DefaultMessages", locale);
		if (this.localization == null) {
			final var mappingProviders = localizationService.getMappingProviders();
			final var mappingReaders = localizationService.getMappingReaders();
			ExceptionsKt.throwInternal("Could not load DefaultMessages, providers: " + mappingProviders + ", readers: " + mappingReaders);
		}
	}

	@NotNull
	private LocalizationTemplate getLocalizationTemplate(@NotNull String path) {
		final LocalizationTemplate template = getLocalizationTemplateOrNull(path);
		if (template == null) {
			ExceptionsKt.throwInternal("Localization template for default messages '" + path + "' could not be found, available keys: " + localization.getKeys());
			throw new AssertionError();
		}

		return template;
	}

	@Nullable
	private LocalizationTemplate getLocalizationTemplateOrNull(@NotNull String path) {
        return localization.get(path);
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
	 * @return Message to display when the command is on per-user rate limit
	 */
	public String getUserRateLimitMsg(Timestamp timestamp) {
		return getLocalizationTemplate("user.rate_limit.message").localize(entry("delay", timestamp));
	}

	/**
	 * @return Message to display when the command is on per-channel rate limit
	 */
	public String getChannelRateLimitMsg(Timestamp timestamp) {
		return getLocalizationTemplate("channel.rate_limit.message").localize(entry("delay", timestamp));
	}

	/**
	 * @return Message to display when the command is on per-guild rate limit
	 */
	public String getGuildRateLimitMsg(Timestamp timestamp) {
		return getLocalizationTemplate("guild.rate_limit.message").localize(entry("delay", timestamp));
	}

	/**
	 * @return Message to display when the command is not found
	 */
	public String getCommandNotFoundMsg(String suggestions) {
		return getLocalizationTemplate("command.not.found.message").localize(entry("suggestions", suggestions));
	}

	/**
	 * @return Message to display when a channel parameter could not be resolved
	 */
	public String getResolverChannelNotFoundMsg() {
		return getLocalizationTemplate("resolver.channel.not_found.message").localize();
	}

	/**
	 * @return Message to display when a user parameter could not be resolved
	 */
	public String getResolverUserNotFoundMsg() {
		return getLocalizationTemplate("resolver.user.not_found.message").localize();
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
