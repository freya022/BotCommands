package com.freya02.botcommands.api.application.slash;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.localization.Localization;
import com.freya02.botcommands.api.localization.annotations.LocalizationBundle;
import com.freya02.botcommands.internal.BContextImpl;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.internal.interactions.command.SlashCommandInteractionImpl;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Locale;

public abstract class GlobalSlashEvent extends SlashCommandInteractionEvent {
	protected final Method method;

	public GlobalSlashEvent(@NotNull Method method, @NotNull JDA api, long responseNumber, @NotNull SlashCommandInteractionImpl interaction) {
		super(api, responseNumber, interaction);

		this.method = method;
	}

	public abstract BContext getContext();

	//TODO move to same pattern as slash command interactions, to reduce duplication while keeping functionality separation
	// => (UserLocalizable, GuildLocalizable) => Localizable (?)
	@NotNull
	public abstract String localize(@NotNull Locale locale, @NotNull String localizationBundle, @NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries);

	@NotNull
	public String localize(@NotNull Locale locale, @NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries) {
		return localize(locale, getLocalizationBundle(), localizationPath, entries);
	}

	@NotNull
	public String localizeGuild(@NotNull String localizationBundle, @NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries) {
		return localize(getGuildLocale(), localizationBundle, localizationPath, entries);
	}

	@NotNull
	public String localizeGuild(@NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries) {
		return localize(getGuildLocale(), getLocalizationBundle(), localizationPath, entries);
	}

	@NotNull
	public String localizeUser(@NotNull String localizationBundle, @NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries) {
		return localize(getUserLocale(), localizationBundle, localizationPath, entries);
	}

	@NotNull
	public String localizeUser(@NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries) {
		return localize(getUserLocale(), getLocalizationBundle(), localizationPath, entries);
	}

	@NotNull
	private String getLocalizationBundle() {
		final String localizationBundle = ((BContextImpl) getContext()).getLocalizationManager().getLocalizationBundle(method);

		if (localizationBundle == null) {
			throw new IllegalArgumentException("You cannot use this localization method without having the command, or the class which contains it, be annotated with @" + LocalizationBundle.class.getSimpleName());
		}
		return localizationBundle;
	}
}
