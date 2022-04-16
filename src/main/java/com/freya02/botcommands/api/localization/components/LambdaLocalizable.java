package com.freya02.botcommands.api.localization.components;

import com.freya02.botcommands.api.localization.GuildLocalizable;
import com.freya02.botcommands.api.localization.Localizable;
import com.freya02.botcommands.api.localization.Localization;
import com.freya02.botcommands.api.localization.UserLocalizable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public interface LambdaLocalizable extends GuildLocalizable, UserLocalizable, Localizable {
	/**
	 * <b>Localization path prefix set on method / class will not be taken into account if the event comes from a lambda</b>
	 *
	 * {@inheritDoc}
	 */
	@Override
	@NotNull
	String localizeGuild(@NotNull String localizationBundle, @NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries);

	/**
	 * <b>Will not work under lambdas</b>
	 * <br><b>Localization path prefix set on method / class will not be taken into account if the event comes from a lambda</b>
	 *
	 * {@inheritDoc}
	 */
	@Override
	@NotNull
	String localizeGuild(@NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries);

	/**
	 * <b>Localization path prefix set on method / class will not be taken into account if the event comes from a lambda</b>
	 *
	 * {@inheritDoc}
	 */
	@Override
	@NotNull
	String localizeUser(@NotNull String localizationBundle, @NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries);

	/**
	 * <b>Will not work under lambdas</b>
	 * <br><b>Localization path prefix set on method / class will not be taken into account if the event comes from a lambda</b>
	 *
	 * {@inheritDoc}
	 */
	@Override
	@NotNull
	String localizeUser(@NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries);

	/**
	 * <b>Localization path prefix set on method / class will not be taken into account if the event comes from a lambda</b>
	 *
	 * {@inheritDoc}
	 */
	@Override
	@NotNull
	String localize(@NotNull Locale locale, @NotNull String localizationBundle, @NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries);

	/**
	 * <br><b>Localization path prefix set on method / class will not be taken into account if the event comes from a lambda</b>
	 *
	 * {@inheritDoc}
	 */
	@Override
	@NotNull
	String localize(@NotNull String localizationBundle, @NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries);

	/**
	 * <b>Will not work under lambdas</b>
	 * <br><b>Localization path prefix set on method / class will not be taken into account if the event comes from a lambda</b>
	 *
	 * {@inheritDoc}
	 */
	@Override
	@NotNull
	String localize(@NotNull Locale locale, @NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries);

	/**
	 * <b>Will not work under lambdas</b>
	 * <br><b>Localization path prefix set on method / class will not be taken into account if the event comes from a lambda</b>
	 *
	 * {@inheritDoc}
	 */
	@Override
	@NotNull
	String localize(@NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries);

	/**
	 * <b>Will not work under lambdas</b>
	 *
	 * {@inheritDoc}
	 * @return
	 */
	@Override
	@NotNull
	String getLocalizationBundle();
}
