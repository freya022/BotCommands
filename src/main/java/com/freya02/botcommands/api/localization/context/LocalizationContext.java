package com.freya02.botcommands.api.localization.context;

import com.freya02.botcommands.api.localization.Localization;
import com.freya02.botcommands.api.localization.annotations.LocalizationOptions;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface helping in localizing content, supports preset localization bundles,
 * localization prefixes and context-aware localization.
 */
public interface LocalizationContext {
    /**
     * Returns the localization bundle of the current context.
     * <br>The localization bundle can either come from {@link LocalizationOptions#bundle()} from {@link #withBundle(String)}.
     *
     * @return The localization bundle for this context
     *
     * @see #withBundle(String)
     */
    @NotNull
    String getLocalizationBundle();

    /**
     * Returns the localization prefix of the current context.
     * <br>The localization prefix can either come from {@link LocalizationOptions#prefix()} from {@link #withPrefix(String)}.
     *
     * @return The localization prefix for this context, or {@code null} if none has been set
     *
     * @see #withPrefix(String)
     */
    @Nullable
    String getLocalizationPrefix();

    //TODO docs
    @NotNull
    TextLocalizationContext withGuildLocale(@NotNull DiscordLocale guildLocale);

    //TODO docs
    @NotNull
    AppLocalizationContext withUserLocale(@NotNull DiscordLocale userLocale);

    //TODO docs
    @NotNull
    LocalizationContext withBundle(@NotNull String localizationBundle);

    //TODO docs
    @NotNull
    LocalizationContext withPrefix(@NotNull String localizationPrefix);

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
     * @param locale             The DiscordLocale to use when fetching the localization bundle
     * @param localizationPath   The localization path to search for
     * @param entries            The entries to fill the template
     *
     * @return The localized string
     */
    @NotNull
    String localize(@NotNull DiscordLocale locale, @NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries);

    /**
     * Localizes the provided path, in the current context's bundle, with the best locale available (User > Guild > Default)
     *
     * @param localizationPath   The localization path to search for
     * @param entries            The entries to fill the template
     *
     * @return The localized string
     */
    @NotNull
    String localize(@NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries);
}
