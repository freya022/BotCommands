package io.github.freya022.botcommands.api.localization;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public record LocalizationMapRequest(@NotNull String baseName, @NotNull Locale requestedLocale, @NotNull String bundleName) {

}
