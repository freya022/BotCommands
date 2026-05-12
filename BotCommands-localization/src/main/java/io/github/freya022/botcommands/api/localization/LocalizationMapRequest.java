package io.github.freya022.botcommands.api.localization;

import org.jspecify.annotations.NullMarked;

import java.util.Locale;

@NullMarked
public record LocalizationMapRequest(String baseName, Locale requestedLocale,
                                     String bundleName) {

}
