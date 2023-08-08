package com.freya02.botcommands.api.localization.providers;

import com.freya02.botcommands.api.core.service.annotations.BService;
import com.freya02.botcommands.api.localization.LocalizationMap;
import com.freya02.botcommands.api.localization.LocalizationTemplate;
import com.freya02.botcommands.api.localization.readers.DefaultJsonLocalizationMapReader;
import com.freya02.botcommands.api.localization.readers.LocalizationMapReaders;
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
		final LocalizationMap localizationMap = localizationMapReaders.cycleReaders(baseName, requestedLocale);

		return withParentBundles(baseName, requestedLocale, localizationMap);
	}

	@Nullable
	@Override
	public LocalizationMap fromBundle(@NotNull String baseName, @NotNull Locale locale) {
        return localizationMapReaders.cycleReaders(baseName, locale);
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
                localizationMap = createDelegated(localizationMap, parentLocalizationMap);
            }
		}

		return localizationMap;
	}

	private LocalizationMap createDelegated(@Nullable LocalizationMap current, @NotNull LocalizationMap parent) {
		final Locale effectiveLocale = current != null ? current.getEffectiveLocale() : parent.getEffectiveLocale();
		return new LocalizationMap() {
			@NotNull
			@Override
			public Locale getEffectiveLocale() {
				return effectiveLocale;
			}

			@Nullable
			@Override
			public LocalizationTemplate get(@NotNull String path) {
				if (current != null) {
					final LocalizationTemplate template = current.get(path);
					if (template != null)
						return template;
				}

				return parent.get(path);
			}
		};
	}
}
