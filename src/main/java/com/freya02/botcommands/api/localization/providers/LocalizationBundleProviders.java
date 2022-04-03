package com.freya02.botcommands.api.localization.providers;

import com.freya02.botcommands.api.localization.LocalizationBundle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.io.IOException;
import java.util.*;

public final class LocalizationBundleProviders {
	private static final Set<LocalizationBundleProvider> providers = new HashSet<>();

	@Contract(pure = true)
	@NotNull
	@UnmodifiableView
	public static Collection<LocalizationBundleProvider> getProviders() {
		return Collections.unmodifiableSet(providers);
	}

	@Nullable
	public static LocalizationBundle cycleProviders(@NotNull String baseName, @NotNull Locale locale) throws IOException {
		for (LocalizationBundleProvider provider : providers) {
			final LocalizationBundle bundle = provider.getBundle(baseName, locale);

			if (bundle != null) {
				return bundle;
			}
		}

		return null;
	}

	public static void registerProvider(@NotNull LocalizationBundleProvider provider) {
		providers.add(provider);
	}

	static {
		registerProvider(new DefaultLocalizationBundleProvider());
	}
}
