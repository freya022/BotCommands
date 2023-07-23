package com.freya02.botcommands.api.localization;

import com.freya02.botcommands.api.localization.providers.DefaultLocalizationMapProvider;
import com.freya02.botcommands.api.localization.providers.LocalizationMapProvider;
import com.freya02.botcommands.api.localization.providers.LocalizationMapProviders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Locale;
import java.util.Map;

/**
 * Provides a low level API for localization.
 * <br>You can get an instance using {@link #getInstance(String, Locale)}, as well as invalidate cached localization data, as to reload them on next use.
 * <br>You can customize localization providers, as well as the localization templates they give, each provider is tested until one returns a valid localization bundle, see {@link DefaultLocalizationMapProvider} for the default specification.
 * <p>
 * You can add more localization bundle providers using {@link LocalizationMapProviders#registerProvider(LocalizationMapProvider)}.
 */
public interface Localization {
    /**
     * Returns an unmodifiable view of the {@code localization path -> LocalizationTemplate} map
     *
     * @return An unmodifiable view of the {@code localization path -> LocalizationTemplate} map
     */
    @NotNull
    @UnmodifiableView
    Map<String, ? extends LocalizationTemplate> getTemplateMap();

    /**
     * Returns the {@link LocalizationTemplate} for the specified localization path
     *
     * @param path The localization path of the template
     *
     * @return The {@link LocalizationTemplate} for the specified localization path
     */
    @Nullable
    LocalizationTemplate get(@NotNull String path);

    /**
     * Returns the effective Locale for this Localization instance
     * <br>This might not be the same as the one supplied in {@link #getInstance(String, Locale)} due to missing bundles
     *
     * @return The effective Locale for this Localization instance
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
         *
         * @return The entry
         */
        @NotNull
        public static Entry entry(@NotNull String key, @NotNull Object value) {
            return new Entry(key, value);
        }
    }
}
