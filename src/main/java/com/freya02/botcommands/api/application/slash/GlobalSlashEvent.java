package com.freya02.botcommands.api.application.slash;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.localization.GuildLocalizable;
import com.freya02.botcommands.api.localization.Localizable;
import com.freya02.botcommands.api.localization.Localization;
import com.freya02.botcommands.api.localization.UserLocalizable;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.localization.EventLocalizer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.internal.interactions.command.SlashCommandInteractionImpl;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Locale;

public abstract class GlobalSlashEvent extends SlashCommandInteractionEvent implements GuildLocalizable, UserLocalizable, Localizable {
	private final EventLocalizer localizer;

	public GlobalSlashEvent(@NotNull BContextImpl context, @NotNull Method method, @NotNull JDA api, long responseNumber, @NotNull SlashCommandInteractionImpl interaction) {
		super(api, responseNumber, interaction);

		this.localizer = new EventLocalizer(context, method, isFromGuild() ? interaction.getGuildLocale() : null, interaction.getUserLocale());
	}

	public abstract BContext getContext();

	@Override
	@NotNull
	public String localizeGuild(@NotNull String localizationBundle, @NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries) {return localizer.localizeGuild(localizationBundle, localizationPath, entries);}

	@Override
	@NotNull
	public String localizeGuild(@NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries) {return localizer.localizeGuild(localizationPath, entries);}

	@Override
	@NotNull
	public String localizeUser(@NotNull String localizationBundle, @NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries) {return localizer.localizeUser(localizationBundle, localizationPath, entries);}

	@Override
	@NotNull
	public String localizeUser(@NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries) {return localizer.localizeUser(localizationPath, entries);}

	@Override
	@NotNull
	public String localize(@NotNull Locale locale, @NotNull String localizationBundle, @NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries) {return localizer.localize(locale, localizationBundle, localizationPath, entries);}

	@Override
	@NotNull
	public String localize(@NotNull String localizationBundle, @NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries) {return localizer.localize(localizationBundle, localizationPath, entries);}

	@Override
	@NotNull
	public String localize(@NotNull Locale locale, @NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries) {return localizer.localize(locale, localizationPath, entries);}

	@Override
	@NotNull
	public String localize(@NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries) {return localizer.localize(localizationPath, entries);}

	@Override
	@NotNull
	public String getLocalizationBundle() {return localizer.getLocalizationBundle();}
}
