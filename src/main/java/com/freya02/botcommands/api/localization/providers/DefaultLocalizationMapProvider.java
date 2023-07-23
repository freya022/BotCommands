package com.freya02.botcommands.api.localization.providers;

import com.freya02.botcommands.api.core.service.annotations.BService;
import com.freya02.botcommands.api.core.service.annotations.ServiceType;
import com.freya02.botcommands.api.localization.DefaultLocalizationMap;
import com.freya02.botcommands.api.localization.LocalizationMap;
import com.freya02.botcommands.api.localization.LocalizationTemplate;
import com.freya02.botcommands.api.localization.readers.DefaultJsonLocalizationMapReader;
import com.freya02.botcommands.api.localization.readers.LocalizationMapReaders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
@ServiceType(types = LocalizationMapProvider.class)
public class DefaultLocalizationMapProvider implements LocalizationMapProvider {
	private final LocalizationMapProviders localizationMapProviders;
	private final LocalizationMapReaders localizationMapReaders;

	public DefaultLocalizationMapProvider(LocalizationMapProviders localizationMapProviders, LocalizationMapReaders localizationMapReaders) {
		this.localizationMapProviders = localizationMapProviders;
		this.localizationMapReaders = localizationMapReaders;
	}

	@Nullable
	@Override
	public LocalizationMap fromBundleOrParent(@NotNull String baseName, @NotNull Locale effectiveLocale) {
		final Map<String, LocalizationTemplate> templateMap = localizationMapReaders.cycleReaders(baseName, effectiveLocale);

		return withParentBundles(baseName, effectiveLocale, templateMap);
	}

	@Nullable
	@Override
	public LocalizationMap fromBundle(@NotNull String baseName, @NotNull Locale locale) {
		final Map<String, LocalizationTemplate> map = localizationMapReaders.cycleReaders(baseName, locale);
		if (map == null) return null;

		return new DefaultLocalizationMap(locale, map);
	}

	@Nullable
	private LocalizationMap withParentBundles(@NotNull String baseName, @NotNull Locale effectiveLocale, @Nullable Map<String, LocalizationTemplate> templateMap) {
		//Need to get parent bundles
		final List<Locale> candidateLocales = CONTROL.getCandidateLocales(baseName, effectiveLocale);

		//Most precise locales are inserted first, if the key isn't already bound to something
		// If the key is already bound then it is coming from the most precise bundle already, so no need to ever replace it
		for (Locale candidateLocale : candidateLocales) {
			if (candidateLocale.equals(effectiveLocale)) continue;

			//Do not use Localization, as it will **also** try to get the parent localizations
			final LocalizationMap parentLocalization = localizationMapProviders.cycleProviders(baseName, candidateLocale);
			if (parentLocalization != null) {
				final Map<String, ? extends LocalizationTemplate> parentTemplateMap = parentLocalization.templateMap();

				if (templateMap == null) {
					templateMap = new HashMap<>();
					effectiveLocale = candidateLocale;
				}

				for (Map.Entry<String, ? extends LocalizationTemplate> entry : parentTemplateMap.entrySet()) {
					templateMap.putIfAbsent(entry.getKey(), entry.getValue());
				}
			}
		}

		if (templateMap == null) {
			return null;
		}

		return new DefaultLocalizationMap(effectiveLocale, templateMap);
	}
}
