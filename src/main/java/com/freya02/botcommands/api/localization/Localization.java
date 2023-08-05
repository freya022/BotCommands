package com.freya02.botcommands.api.localization;

import com.freya02.botcommands.api.localization.providers.LocalizationMapProvider;
import com.freya02.botcommands.api.localization.readers.LocalizationMapReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Low-level interface for localization.
 *
 * <br>You can get an instance using {@link LocalizationService#getInstance(String, Locale)}.
 *
 * <p>You can customize how localization bundles are loaded, which container and template formats are supported
 * with {@link LocalizationMapProvider}, {@link LocalizationMapReader} and {@link LocalizationTemplate}.
 *
 * @see LocalizationMapProvider
 * @see LocalizationMapReader
 * @see LocalizationTemplate
 */
public interface Localization {
    /**
     * Returns the {@link LocalizationTemplate} for the specified localization path
     *
     * @param path The localization path of the template
     */
    @Nullable
    LocalizationTemplate get(@NotNull String path);

    /**
     * Returns the effective Locale for this Localization instance.
     *
     * <p><b>Note:</b> this might not be the locale as provided
     * in {@link LocalizationService#getInstance(String, Locale)} due to missing bundles/unsupported locales.
     */
    @NotNull
    Locale getEffectiveLocale();

    record Entry(@NotNull String argumentName, @NotNull Object value) {
        /**
         * Create a new localization entry,
         * this binds a {@link LocalizationTemplate localization template} argument with the value.
         *
         * <p>I recommend using this method with a static import.
         *
         * @param argumentName The name of the argument from the templated string
         * @param value        The value to assign it to
         */
        @NotNull
        public static Entry entry(@NotNull String argumentName, @NotNull Object value) {
            return new Entry(argumentName, value);
        }
    }
}
