package com.freya02.botcommands.api.localization.providers;

import com.freya02.botcommands.api.localization.*;
import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Default localization bundle provider
 * <p><b>Specification:</b>
 * <br>Localization bundles are read from "<code>/bc_localization/</code>", so, a "<code>bc_localization</code>" directory, in the root of your resources
 * <br>Those localization bundles are in the JSON format and can have any name, with the extension being <code>.json</code>
 * <br>The JSON format work the same as java's {@link ResourceBundle}, you can provide localization entries such as "<code>"my_command.name": "my_command_in_en_US"</code>"
 * <br>But you can also use nesting as a way to not copy the same path prefix everytime, such as:
 * <pre><code>
 *     {
 *         "my_command": {
 *             "name": "my_command_in_en_US",
 *             "description": "my_command_description_in_en_US"
 *         }
 *     }
 * </code></pre>
 * <p>
 * About localization bundle loading:
 * <br>The initial file to be loaded will be the one mentioned above, parent localization bundles may be loaded from other providers, as all providers are tested with {@link LocalizationMapProviders#cycleProvidersNoParent(String, Locale)}
 *
 * <br>See {@link DefaultLocalizationTemplate} for what the localization templates should look like
 *
 * @see DefaultLocalizationTemplate
 */
public class DefaultLocalizationMapProvider implements LocalizationMapProvider {
	@Override
	@Nullable
	public LocalizationMap getBundle(@NotNull String baseName, @NotNull Locale effectiveLocale) throws IOException {
		final Map<String, LocalizationTemplate> templateMap = readTemplateMap(baseName, effectiveLocale);

		return withParentBundles(baseName, effectiveLocale, templateMap);
	}

	@Override
	@Nullable
	public LocalizationMap getBundleNoParent(@NotNull String baseName, @NotNull Locale locale) throws IOException {
		final Map<String, LocalizationTemplate> map = readTemplateMap(baseName, locale);
		if (map == null) return null;

		return new DefaultLocalizationMap(locale, map);
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
	private LocalizationMap withParentBundles(@NotNull String baseName, @NotNull Locale effectiveLocale, @Nullable Map<String, LocalizationTemplate> templateMap) throws IOException {
		//Need to get parent bundles
		final List<Locale> candidateLocales = CONTROL.getCandidateLocales(baseName, effectiveLocale);

		//Most precise locales are inserted first, if the key isn't already bound to something
		// If the key is already bound then it is coming from the most precise bundle already, so no need to ever replace it
		for (Locale candidateLocale : candidateLocales) {
			if (candidateLocale.equals(effectiveLocale)) continue;

			final LocalizationMap parentLocalization = LocalizationMapProviders.cycleProvidersNoParent(baseName, candidateLocale); //Do not try to use Localization which will **also** try to get the parent localizations
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

				final LocalizationTemplate value = new DefaultLocalizationTemplate((String) entry.getValue(), effectiveLocale);

				if (templateMap.put(key, value) != null) {
					throw new IllegalStateException("Got two same localization keys: '" + key + "'");
				}
			}
		}
	}
}
