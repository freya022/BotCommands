package io.github.freya022.botcommands.api.localization.providers;

import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService;
import io.github.freya022.botcommands.api.localization.LocalizationMap;
import io.github.freya022.botcommands.api.localization.readers.LocalizationMapReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Supplies a {@link LocalizationMap} for the requested bundle name and locale.
 * <br>This is an intermediary step, often using existing {@link LocalizationMapReader}s.
 *
 * <p>
 * <b>Usage</b>: Register your instance as a service with {@link BService}.
 *
 * @see DefaultLocalizationMapProvider
 * @see InterfacedService @InterfacedService
 */
@InterfacedService(acceptMultiple = true)
public interface LocalizationMapProvider {
    ResourceBundle.Control CONTROL = ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT);

    /**
     * Loads a localization map with the requested name and requested locale.
     * <br>This may return a localization bundle with a broader locale,
     * which will be returned by {@link LocalizationMap#getEffectiveLocale()}.
     * <br>This may include parent bundles.
     *
     * @param baseName        The base name of the localization bundle
     * @param requestedLocale The requested locale
     *
     * @return A {@link LocalizationMap} instance with the requested data,
     * or {@code null} if no bundle could be read, or a (logged) exception happened.
     */
    @Nullable
    LocalizationMap fromBundleOrParent(@NotNull String baseName, @NotNull Locale requestedLocale);

    /**
     * Loads a localization map with the requested name and requested locale.
     * <br>This may return a localization bundle with a broader locale,
     * which must be returned by {@link LocalizationMap#getEffectiveLocale()}.
     *
     * <p><b>Note:</b> this does <b>NOT</b> include parent bundles.
     *
     * @param baseName        The base name of the localization bundle
     * @param requestedLocale The requested locale
     *
     * @return A {@link LocalizationMap} instance with the requested data,
     * or {@code null} if no bundle could be read, or a (logged) exception happened.
     */
    @Nullable
    LocalizationMap fromBundle(@NotNull String baseName, @NotNull Locale requestedLocale);
}
