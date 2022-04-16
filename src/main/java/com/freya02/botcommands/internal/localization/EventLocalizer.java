package com.freya02.botcommands.internal.localization;

import com.freya02.botcommands.api.localization.*;
import com.freya02.botcommands.api.localization.annotations.LocalizationBundle;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.LocalizationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Locale;

public class EventLocalizer implements UserLocalizable, GuildLocalizable, Localizable {
	private final BContextImpl context;
	private final Method method;

	private final Locale guildLocale;
	private final Locale userLocale;

	public EventLocalizer(@NotNull BContextImpl context, @Nullable Method method, @Nullable Locale guildLocale, @Nullable Locale userLocale) {
		this.context = context;
		this.method = method;

		this.guildLocale = guildLocale;
		this.userLocale = userLocale;
	}

	@Override
	@NotNull
	public String localize(@NotNull Locale locale, @NotNull String localizationBundle, @NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries) {
		final LocalizationManager localizationManager = context.getLocalizationManager();

		final Localization instance = Localization.getInstance(localizationBundle, locale);

		if (instance == null) {
			throw new IllegalArgumentException("Found no localization instance for bundle '%s' and locale '%s'".formatted(localizationBundle, locale));
		}

		final String effectivePath;
		if (method != null) {
			final String localizationPrefix = localizationManager.getLocalizationPrefix(method);

			if (localizationPrefix == null) {
				effectivePath = localizationPath;
			} else {
				effectivePath = localizationPrefix + "." + localizationPath;
			}
		} else {
			effectivePath = localizationPath;
		}

		final LocalizationTemplate template = instance.get(effectivePath);

		if (template == null) {
			throw new IllegalArgumentException("Found no localization template for '%s' (in bundle '%s' with locale '%s')".formatted(effectivePath, localizationBundle, instance.getEffectiveLocale()));
		}

		return template.localize(entries);
	}

	@Override
	@NotNull
	public String localize(@NotNull String localizationBundle, @NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries) {
		if (userLocale != null) {
			return localizeUser(localizationPath, localizationBundle, entries);
		} else if (guildLocale != null) {
			return localizeGuild(localizationPath, localizationBundle, entries);
		} else {
			return localize(Locale.getDefault(), localizationBundle, localizationPath, entries);
		}
	}

	@Override
	@NotNull
	public String localize(@NotNull Locale locale, @NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries) {
		return localize(locale, getLocalizationBundle(), localizationPath, entries);
	}

	@Override
	@NotNull
	public String localize(@NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries) {
		if (userLocale != null) {
			return localizeUser(localizationPath, entries);
		} else if (guildLocale != null) {
			return localizeGuild(localizationPath, entries);
		} else {
			return localize(Locale.getDefault(), localizationPath, entries);
		}
	}

	@Override
	@NotNull
	public String getLocalizationBundle() {
		if (method == null) {
			throw new IllegalStateException("Cannot use predefined localization bundles in this event");
		}

		final String localizationBundle = context.getLocalizationManager().getLocalizationBundle(method);

		if (localizationBundle == null) {
			throw new IllegalArgumentException("You cannot use this localization method without having the command, or the class which contains it, be annotated with @" + LocalizationBundle.class.getSimpleName());
		}

		return localizationBundle;
	}

	@Override
	@NotNull
	public Locale getGuildLocale() {
		if (guildLocale == null)
			throw new IllegalStateException("Cannot guild localize on an event which doesn't provide guild localization");

		return guildLocale;
	}

	@Override
	@NotNull
	public Locale getUserLocale() {
		if (userLocale == null)
			throw new IllegalStateException("Cannot guild localize on an event which doesn't provide guild localization");

		return userLocale;
	}
}
