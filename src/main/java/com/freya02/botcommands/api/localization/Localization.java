package com.freya02.botcommands.api.localization;

import com.freya02.botcommands.api.localization.providers.LocalizationMapProvider;
import com.freya02.botcommands.api.localization.readers.LocalizationMapReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Locale;
import java.util.Map;

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
     * Returns an unmodifiable view of the {@code localization path -> LocalizationTemplate} map
     */
    @NotNull
    @UnmodifiableView
    Map<String, ? extends LocalizationTemplate> getTemplateMap();

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

    record Entry(@NotNull String key, @NotNull Object value) {
        /**
         * Create a new localization entry, this binds a key (from a templated string) into a value
         * <b>Highly recommended to use this method with a static import</b>
         *
         * @param key   The key from the templated string
         * @param value The value to assign it to
         */
        @NotNull
        public static Entry entry(@NotNull String key, @NotNull Object value) {
            return new Entry(key, value);
        }
    }
}
