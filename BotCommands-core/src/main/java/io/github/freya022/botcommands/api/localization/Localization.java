package io.github.freya022.botcommands.api.localization;

import io.github.freya022.botcommands.api.localization.providers.LocalizationMapProvider;
import io.github.freya022.botcommands.api.localization.readers.LocalizationMapReader;
import org.jetbrains.annotations.NotNull;

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
public interface Localization extends LocalizationMap {
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
