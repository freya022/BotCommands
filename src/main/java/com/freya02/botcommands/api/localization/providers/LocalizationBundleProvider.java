package com.freya02.botcommands.api.localization.providers;

import com.freya02.botcommands.api.localization.LocalizationBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public interface LocalizationBundleProvider {
	ResourceBundle.Control CONTROL = ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT);

	default String appendPath(String path, String other) {
		if (path.isBlank()) return other;

		return path + '.' + other;
	}

	@NotNull
	default String getBundleName(@NotNull String baseName, @NotNull Locale locale) {
		return CONTROL.toBundleName(baseName, locale);
	}

	@Nullable
	LocalizationBundle getBundle(@NotNull String baseName, @NotNull Locale locale) throws IOException;
}
