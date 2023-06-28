package com.freya02.botcommands.api.localization.context;

import com.freya02.botcommands.api.localization.Localization;
import com.freya02.botcommands.api.localization.annotations.LocalizationBundle;
import com.freya02.botcommands.internal.localization.LocalizationContextImpl;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.CheckReturnValue;

/**
 * Interface helping in localizing content, supports preset localization bundles,
 * localization prefixes and context-aware localization.
 *
 * <p>While this interface cannot be injected, sub-interfaces can.
 *
 * @see TextLocalizationContext
 * @see AppLocalizationContext
 * @see #create(String, String)
 */
public interface LocalizationContext {
    static LocalizationContext create(@NotNull String localizationBundle,
                                      @Nullable String localizationPrefix) {
        return new LocalizationContextImpl(localizationBundle, localizationPrefix, null, null);
    }

    static TextLocalizationContext create(@NotNull String localizationBundle,
                                          @Nullable String localizationPrefix,
                                          @Nullable DiscordLocale guildLocale) {
        return new LocalizationContextImpl(localizationBundle, localizationPrefix, guildLocale, null);
    }

    static AppLocalizationContext create(@NotNull String localizationBundle,
                                         @Nullable String localizationPrefix,
                                         @Nullable DiscordLocale guildLocale,
                                         @Nullable DiscordLocale userLocale) {
        return new LocalizationContextImpl(localizationBundle, localizationPrefix, guildLocale, userLocale);
    }

    @NotNull
    DiscordLocale getEffectiveLocale();

    /**
     * Returns the localization bundle of the current context.
     * <br>The localization bundle can either come from {@link LocalizationBundle#value()} from {@link #withBundle(String)}.
     *
     * @return The localization bundle for this context
     *
     * @see #withBundle(String)
     */
    @NotNull
    String getLocalizationBundle();

    /**
     * Returns the localization prefix of the current context.
     * <br>The localization prefix can either come from {@link LocalizationBundle#prefix()} from {@link #withPrefix(String)}.
     *
     * @return The localization prefix for this context, or {@code null} if none has been set
     *
     * @see #withPrefix(String)
     */
    @Nullable
    String getLocalizationPrefix();

    /**
     * Returns a new {@link TextLocalizationContext} with the specified guild locale.
     *
     * @param guildLocale The guild locale to use, or {@code null} to remove it
     *
     * @return the new {@link TextLocalizationContext}
     */
    @NotNull
    @CheckReturnValue
    TextLocalizationContext withGuildLocale(@Nullable DiscordLocale guildLocale);

    /**
     * Returns a new {@link AppLocalizationContext} with the specified user locale.
     *
     * @param userLocale The user locale to use, or {@code null} to remove it
     *
     * @return the new {@link AppLocalizationContext}
     */
    @NotNull
    @CheckReturnValue
    AppLocalizationContext withUserLocale(@Nullable DiscordLocale userLocale);

    /**
     * Returns a new localization context with the specified localization bundle.
     *
     * @param localizationBundle The localization bundle to use
     *
     * @return the new localization context
     */
    @NotNull
    @CheckReturnValue
    LocalizationContext withBundle(@NotNull String localizationBundle);

    /**
     * Returns a new localization context with the specified localization prefix.
     *
     * @param localizationPrefix The localization prefix to use, or {@code null} to remove it
     *
     * @return the new localization context
     */
    @NotNull
    @CheckReturnValue
    LocalizationContext withPrefix(@Nullable String localizationPrefix);

    /**
     * Localizes the provided path, in the specified bundle, with the provided locale
     *
     * @param locale             The DiscordLocale to use when fetching the localization bundle
     * @param localizationBundle The name of the localization bundle
     * @param localizationPath   The localization path to search for
     * @param entries            The entries to fill the template
     *
     * @return The localized string
     */
    @NotNull
    String localize(@NotNull DiscordLocale locale, @NotNull String localizationBundle, @NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries);

    /**
     * Localizes the provided path, in the specified bundle, with the best locale available (User > Guild > Default)
     *
     * @param localizationBundle The name of the localization bundle
     * @param localizationPath   The localization path to search for
     * @param entries            The entries to fill the template
     *
     * @return The localized string
     */
    @NotNull
    String localize(@NotNull String localizationBundle, @NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries);

    /**
     * Localizes the provided path, in the current context's bundle, with the provided locale
     *
     * @param locale           The DiscordLocale to use when fetching the localization bundle
     * @param localizationPath The localization path to search for
     * @param entries          The entries to fill the template
     *
     * @return The localized string
     */
    @NotNull
    String localize(@NotNull DiscordLocale locale, @NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries);

    /**
     * Localizes the provided path, in the current context's bundle, with the best locale available (User > Guild > Default)
     *
     * @param localizationPath The localization path to search for
     * @param entries          The entries to fill the template
     *
     * @return The localized string
     */
    @NotNull
    String localize(@NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries);
}
