package com.freya02.botcommands.internal.utils;

import com.freya02.botcommands.internal.Logging;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class BResourceBundle {
	private static final Logger LOGGER = Logging.getLogger();

	private static final Map<String, Map<Locale, BResourceBundle>> cachedBundles = Collections.synchronizedMap(new HashMap<>());

	@NotNull private final ResourceBundle bundle;

	private BResourceBundle(@NotNull ResourceBundle bundle) {
		this.bundle = bundle;
	}

	public static BResourceBundle getBundle(@NotNull String name, @NotNull Locale locale) {
		final AtomicBoolean contained = new AtomicBoolean(true);
		final Map<Locale, BResourceBundle> localeBResourceBundleMap = cachedBundles.computeIfAbsent(name, s -> {
			contained.set(false);

			return Collections.synchronizedMap(new HashMap<>());
		});

		return localeBResourceBundleMap.computeIfAbsent(locale, l -> {
			try {
				final ResourceBundle bundle = ResourceBundle.getBundle(name, locale);

				if (bundle.getLocale().toString().isBlank() && !locale.toString().isBlank()) { //Check if the bundle loaded is not a fallback
					if (Locale.getDefault() != locale) { //No need to warn when the bundle choosed is the default one when the locale asked is the default one
						LOGGER.warn("Tried to use a {} bundle with locale '{}' but none was found, using default bundle", name, locale);
					}
				}

				return new BResourceBundle(bundle);
			} catch (MissingResourceException e) {
				if (!contained.get()) { //If the name mapping got created now, only print this warn once
					LOGGER.warn("Can't find resource {}.properties which is used for localized strings, localization won't be used", name);
				}

				return null;
			}
		});
	}

	@NotNull
	public ResourceBundle getBundle() {
		return bundle;
	}

	@NotNull
	public String getValueOrDefault(@NotNull String label, @NotNull String defaultVal) {
		if (!bundle.containsKey(label)) {
			return defaultVal;
		} else {
			return bundle.getString(label);
		}
	}

	@Nullable
	public String getValue(@NotNull String label) {
		if (!bundle.containsKey(label)) {
			return null;
		} else {
			return bundle.getString(label);
		}
	}

	@NotNull
	public String getPathValueOrDefault(@NotNull String defaultVal, @NotNull Object... paths) {
		final String label = getPathLabel(paths);

		if (!bundle.containsKey(label)) {
			return defaultVal;
		} else {
			return bundle.getString(label);
		}
	}

	@Nullable
	public String getPathValue(@NotNull Object... paths) {
		final String label = getPathLabel(paths);

		if (!bundle.containsKey(label)) {
			return null;
		} else {
			return bundle.getString(label);
		}
	}

	private String getPathLabel(Object... paths) {
		final StringJoiner joiner = new StringJoiner(".");

		for (Object o : paths) {
			joiner.add(o.toString());
		}

		return joiner.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		BResourceBundle that = (BResourceBundle) o;

		return bundle.equals(that.bundle);
	}

	@Override
	public int hashCode() {
		return bundle.hashCode();
	}
}
