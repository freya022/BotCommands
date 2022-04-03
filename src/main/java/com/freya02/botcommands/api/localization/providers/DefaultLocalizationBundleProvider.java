package com.freya02.botcommands.api.localization.providers;

import com.freya02.botcommands.api.localization.DefaultLocalizationBundle;
import com.freya02.botcommands.api.localization.Localization;
import com.freya02.botcommands.api.localization.LocalizationBundle;
import com.freya02.botcommands.api.localization.LocalizationTemplate;
import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class DefaultLocalizationBundleProvider implements LocalizationBundleProvider {
	@Override
	@Nullable
	public LocalizationBundle getBundle(@NotNull String baseName, @NotNull Locale effectiveLocale) throws IOException {
		final Map<String, LocalizationTemplate> templateMap = readTemplateMap(baseName, effectiveLocale);

		return withParentBundles(baseName, effectiveLocale, templateMap);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	private Map<String, LocalizationTemplate> readTemplateMap(@NotNull String baseName, @NotNull Locale effectiveLocale) throws IOException {
		final InputStream stream = Localization.class.getResourceAsStream("/bc_localization/" + getBundleName(baseName, effectiveLocale) + ".json");
		if (stream == null) {
			return null;
		}

		final Map<String, LocalizationTemplate> templateMap = new HashMap<>();

		try (InputStreamReader reader = new InputStreamReader(stream)) {
			final Map<String, ?> map = new Gson().fromJson(reader, Map.class);

			discoverEntries(templateMap, baseName, effectiveLocale, "", map.entrySet());
		}

		return templateMap;
	}

	@Nullable
	private LocalizationBundle withParentBundles(@NotNull String baseName, @NotNull Locale effectiveLocale, @Nullable Map<String, LocalizationTemplate> templateMap) throws IOException {
		//Need to get parent bundles
		final List<Locale> candidateLocales = CONTROL.getCandidateLocales(baseName, effectiveLocale);

		//Most precise locales are inserted first, if the key isn't already bound to something
		// If the key is already bound then it is coming from the most precise bundle already, so no need to ever replace it
		for (Locale candidateLocale : candidateLocales) {
			if (candidateLocale.equals(effectiveLocale)) continue;

			final Map<String, LocalizationTemplate> parentTemplateMap = readTemplateMap(baseName, candidateLocale);
			if (parentTemplateMap != null) {
				if (templateMap == null) {
					templateMap = new HashMap<>();
				}

				for (Map.Entry<String, LocalizationTemplate> entry : parentTemplateMap.entrySet()) {
					templateMap.putIfAbsent(entry.getKey(), entry.getValue());
				}
			}
		}

		if (templateMap == null) {
			return null;
		}

		return new DefaultLocalizationBundle(effectiveLocale, templateMap);
	}

	@SuppressWarnings("unchecked")
	private void discoverEntries(Map<String, LocalizationTemplate> templateMap, @NotNull String baseName, Locale effectiveLocale, String currentPath, Set<? extends Map.Entry<String, ?>> entries) {
		for (Map.Entry<String, ?> entry : entries) {
			final String key = appendPath(currentPath, entry.getKey());

			if (entry.getValue() instanceof Map<?, ?> map) {
				discoverEntries(
						templateMap,
						baseName,
						effectiveLocale,
						key,
						((Map<String, ?>) map).entrySet()
				);
			} else {
				if (!(entry.getValue() instanceof String))
					throw new IllegalArgumentException("Key '%s' in bundle '%s' (locale '%s') can only be a String".formatted(key, baseName, effectiveLocale));

				final LocalizationTemplate value = new LocalizationTemplate((String) entry.getValue(), effectiveLocale);

				if (templateMap.put(key, value) != null) {
					throw new IllegalStateException("Got two same localization keys: '" + key + "'");
				}
			}
		}
	}
}
