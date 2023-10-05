package io.github.freya022.botcommands.api.localization.providers;

import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.botcommands.api.localization.LocalizationMap;
import io.github.freya022.botcommands.api.localization.LocalizationMapKt;
import io.github.freya022.botcommands.api.localization.readers.DefaultJsonLocalizationMapReader;
import io.github.freya022.botcommands.api.localization.readers.LocalizationMapReaders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

/**
 * Default implementation for {@link LocalizationMap} providers.
 *
 * <p>This provider simply takes care of the loading order and merging of the localization templates.
 * <br>The templates are loaded with the best (most specific or closest) locale available,
 * followed by the templates of broader locales.
 *
 * <p>Additionally, this reads bundles with a {@code -default} postfix on the base name,
 * such as {@code DefaultMessage-default_fr_FR}.
 *
 * @see DefaultJsonLocalizationMapReader
 */
@BService
public class DefaultLocalizationMapProvider implements LocalizationMapProvider {
	private final LocalizationMapProviders localizationMapProviders;
	private final LocalizationMapReaders localizationMapReaders;

	public DefaultLocalizationMapProvider(LocalizationMapProviders localizationMapProviders, LocalizationMapReaders localizationMapReaders) {
		this.localizationMapProviders = localizationMapProviders;
		this.localizationMapReaders = localizationMapReaders;
	}

	@Nullable
	@Override
	public LocalizationMap fromBundleOrParent(@NotNull String baseName, @NotNull Locale requestedLocale) {
		final LocalizationMap localizationMap = fromBundle(baseName, requestedLocale);
		return withParentBundles(baseName, requestedLocale, localizationMap);
	}

	@Nullable
	@Override
	public LocalizationMap fromBundle(@NotNull String baseName, @NotNull Locale requestedLocale) {
		final LocalizationMap localizationMap = localizationMapReaders.cycleReaders(baseName, requestedLocale);
		final LocalizationMap defaultLocalizationMap = localizationMapReaders.cycleReaders(baseName + "-default", requestedLocale);
		if (defaultLocalizationMap != null) {
			return LocalizationMapKt.createDelegated(localizationMap, defaultLocalizationMap);
		}
		return localizationMap;
	}

	@Nullable
	private LocalizationMap withParentBundles(@NotNull String baseName, @NotNull Locale effectiveLocale, @Nullable LocalizationMap localizationMap) {
		//Need to get parent bundles
		final List<Locale> candidateLocales = CONTROL.getCandidateLocales(baseName, effectiveLocale);

		//Most precise locales are inserted first, if the key isn't already bound to something
		// If the key is already bound then it is coming from the most precise bundle already, so no need to ever replace it
		for (Locale candidateLocale : candidateLocales) {
			if (candidateLocale.equals(effectiveLocale)) continue;

			//Do not use Localization, as it will **also** try to get the parent localizations
			final LocalizationMap parentLocalizationMap = localizationMapProviders.cycleProviders(baseName, candidateLocale);
			if (parentLocalizationMap != null) {
                localizationMap = LocalizationMapKt.createDelegated(localizationMap, parentLocalizationMap);
            }
		}

		return localizationMap;
	}
}
