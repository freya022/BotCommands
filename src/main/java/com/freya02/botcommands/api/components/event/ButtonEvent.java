package com.freya02.botcommands.api.components.event;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.localization.GuildLocalizable;
import com.freya02.botcommands.api.localization.Localizable;
import com.freya02.botcommands.api.localization.Localization;
import com.freya02.botcommands.api.localization.UserLocalizable;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.localization.EventLocalizer;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Locale;

public class ButtonEvent extends ButtonInteractionEvent implements GuildLocalizable, UserLocalizable, Localizable {
	private final EventLocalizer localizer;

	private final BContext context;

	public ButtonEvent(@Nullable Method method, BContextImpl context, ButtonInteractionEvent event) {
		super(event.getJDA(), event.getResponseNumber(), event.getInteraction());

		this.context = context;
		this.localizer = new EventLocalizer(context, method, event.getGuildLocale(), event.getUserLocale());
	}

	public BContext getContext() {
		return context;
	}

	/**
	 * <b>Localization path prefix set on method / class will not be taken into account if the event comes from a lambda</b>
	 *
	 * {@inheritDoc}
	 */
	@Override
	@NotNull
	public String localizeGuild(@NotNull String localizationBundle, @NotNull String localizationPath, Localization.@NotNull Entry @NotNull ... entries) {return localizer.localizeGuild(localizationBundle, localizationPath, entries);}

	/**
	 * <b>Will not work under lambdas</b>
	 * <br><b>Localization path prefix set on method / class will not be taken into account if the event comes from a lambda</b>
	 *
	 * {@inheritDoc}
	 */
	@Override
	@NotNull
	public String localizeGuild(@NotNull String localizationPath, Localization.@NotNull Entry @NotNull ... entries) {return localizer.localizeGuild(localizationPath, entries);}

	/**
	 * <b>Localization path prefix set on method / class will not be taken into account if the event comes from a lambda</b>
	 *
	 * {@inheritDoc}
	 */
	@Override
	@NotNull
	public String localizeUser(@NotNull String localizationBundle, @NotNull String localizationPath, Localization.@NotNull Entry @NotNull ... entries) {return localizer.localizeUser(localizationBundle, localizationPath, entries);}

	/**
	 * <b>Will not work under lambdas</b>
	 * <br><b>Localization path prefix set on method / class will not be taken into account if the event comes from a lambda</b>
	 *
	 * {@inheritDoc}
	 */
	@Override
	@NotNull
	public String localizeUser(@NotNull String localizationPath, Localization.@NotNull Entry @NotNull ... entries) {return localizer.localizeUser(localizationPath, entries);}

	/**
	 * <b>Localization path prefix set on method / class will not be taken into account if the event comes from a lambda</b>
	 *
	 * {@inheritDoc}
	 */
	@Override
	@NotNull
	public String localize(@NotNull Locale locale, @NotNull String localizationBundle, @NotNull String localizationPath, Localization.@NotNull Entry @NotNull ... entries) {return localizer.localize(locale, localizationBundle, localizationPath, entries);}

	/**
	 * <b>Will not work under lambdas</b>
	 * <br><b>Localization path prefix set on method / class will not be taken into account if the event comes from a lambda</b>
	 *
	 * {@inheritDoc}
	 */
	@Override
	@NotNull
	public String localize(@NotNull Locale locale, @NotNull String localizationPath, Localization.@NotNull Entry @NotNull ... entries) {return localizer.localize(locale, localizationPath, entries);}

	/**
	 * <b>Will not work under lambdas</b>
	 *
	 * {@inheritDoc}
	 * @return
	 */
	@Override
	@NotNull
	public String getLocalizationBundle() {return localizer.getLocalizationBundle();}
}
