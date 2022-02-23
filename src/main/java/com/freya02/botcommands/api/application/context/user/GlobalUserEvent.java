package com.freya02.botcommands.api.application.context.user;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.localization.GuildLocalizable;
import com.freya02.botcommands.api.localization.Localizable;
import com.freya02.botcommands.api.localization.Localization;
import com.freya02.botcommands.api.localization.UserLocalizable;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.localization.EventLocalizer;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Locale;

public class GlobalUserEvent extends UserContextInteractionEvent implements GuildLocalizable, UserLocalizable, Localizable {
	private final EventLocalizer localizer;

	private final BContext context;

	public GlobalUserEvent(@NotNull Method method, BContextImpl context, UserContextInteractionEvent event) {
		super(event.getJDA(), event.getResponseNumber(), event.getInteraction());

		this.context = context;
		this.localizer = new EventLocalizer(context, method, event.getGuildLocale(), event.getUserLocale());
	}

	@NotNull
	public BContext getContext() {
		return context;
	}

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
