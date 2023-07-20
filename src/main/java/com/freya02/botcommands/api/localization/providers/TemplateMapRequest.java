package com.freya02.botcommands.api.localization.providers;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public record TemplateMapRequest(@NotNull String baseName, @NotNull Locale effectiveLocale, @NotNull String bundleName) {

}
