package com.freya02.botcommands.api.localization.providers;

import com.freya02.botcommands.api.localization.LocalizationMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.io.IOException;
import java.util.*;

//TODO docs
public final class LocalizationMapProviders {
	private static final Set<LocalizationMapProvider> providers = new HashSet<>();

	@Contract(pure = true)
	@NotNull
	@UnmodifiableView
	public static Collection<LocalizationMapProvider> getProviders() {
		return Collections.unmodifiableSet(providers);
	}

	//TODO docs
	@Nullable
	public static LocalizationMap cycleProviders(@NotNull String baseName, @NotNull Locale locale) throws IOException {
		for (LocalizationMapProvider provider : providers) {
			final LocalizationMap bundle = provider.getBundle(baseName, locale);

			if (bundle != null) {
				return bundle;
			}
		}

		return null;
	}

	//TODO docs
	@Nullable
	public static LocalizationMap cycleProvidersNoParent(@NotNull String baseName, @NotNull Locale locale) throws IOException {
		for (LocalizationMapProvider provider : providers) {
			final LocalizationMap bundle = provider.getBundleNoParent(baseName, locale);

			if (bundle != null) {
				return bundle;
			}
		}

		return null;
	}

	public static void registerProvider(@NotNull LocalizationMapProvider provider) {
		providers.add(provider);
	}

	static {
		registerProvider(new DefaultLocalizationMapProvider());
	}
}
