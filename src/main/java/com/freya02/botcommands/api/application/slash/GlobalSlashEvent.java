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

	protected final Method method;

	public GlobalSlashEvent(@NotNull Method method, @NotNull JDA api, long responseNumber, @NotNull SlashCommandInteractionImpl interaction) {
		super(api, responseNumber, interaction);

		this.method = method;
		this.localizer = new EventLocalizer((BContextImpl) getContext(), method, interaction.getGuildLocale(), interaction.getUserLocale());
	}

	public abstract BContext getContext();

	@Override
	@NotNull
	public String localizeGuild(@NotNull String localizationBundle, @NotNull String localizationPath, Localization.@NotNull Entry @NotNull ... entries) {return localizer.localizeGuild(localizationBundle, localizationPath, entries);}

	@Override
	@NotNull
	public String localizeGuild(@NotNull String localizationPath, Localization.@NotNull Entry @NotNull ... entries) {return localizer.localizeGuild(localizationPath, entries);}

	@Override
	@NotNull
	public String localizeUser(@NotNull String localizationBundle, @NotNull String localizationPath, Localization.@NotNull Entry @NotNull ... entries) {return localizer.localizeUser(localizationBundle, localizationPath, entries);}

	@Override
	@NotNull
	public String localizeUser(@NotNull String localizationPath, Localization.@NotNull Entry @NotNull ... entries) {return localizer.localizeUser(localizationPath, entries);}

	@Override
	@NotNull
	public String localize(@NotNull Locale locale, @NotNull String localizationBundle, @NotNull String localizationPath, Localization.@NotNull Entry @NotNull ... entries) {return localizer.localize(locale, localizationBundle, localizationPath, entries);}

	@Override
	@NotNull
	public String localize(@NotNull Locale locale, @NotNull String localizationPath, Localization.@NotNull Entry @NotNull ... entries) {return localizer.localize(locale, localizationPath, entries);}

	@Override
	@NotNull
	public String getLocalizationBundle() {return localizer.getLocalizationBundle();}
}
