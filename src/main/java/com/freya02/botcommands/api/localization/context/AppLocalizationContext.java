package com.freya02.botcommands.api.localization.context;

import com.freya02.botcommands.api.localization.Localization;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.Interaction;
import org.jetbrains.annotations.NotNull;

/**
 * Interface helping in localizing content, supports preset localization bundles,
 * localization prefixes and context-aware localization.
 *
 * <p>This interface also includes the user's and the guild's locale by default.
 *
 * @see #getUserLocale()
 * @see #getGuildLocale()
 */
public interface AppLocalizationContext extends TextLocalizationContext {
    /**
     * Returns the Locale of the user
     * <br>The locale can either come from the {@link Interaction} or from {@link #withGuildLocale(DiscordLocale)}.
     *
     * @return The Locale of the user
     *
     * @see #withUserLocale(DiscordLocale)
     */
    @NotNull //User locale is always provided in interactions
    DiscordLocale getUserLocale();

    @NotNull
    @Override
    AppLocalizationContext withGuildLocale(@NotNull DiscordLocale guildLocale);

    @NotNull
    @Override
    AppLocalizationContext withBundle(@NotNull String localizationBundle);

    @NotNull
    @Override
    AppLocalizationContext withPrefix(@NotNull String localizationPrefix);

    /**
     * Localizes the provided path, in the specified bundle, with the user's locale
     *
     * @param localizationBundle The name of the localization bundle
     * @param localizationPath   The localization path to search for
     * @param entries            The entries to fill the template
     *
     * @return The localized string
     */
    @NotNull
    default String localizeUser(@NotNull String localizationBundle, @NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries) {
        return localize(getUserLocale(), localizationBundle, localizationPath, entries);
    }

    /**
     * Localizes the provided path, in the current context's bundle, with the user's locale
     *
     * @param localizationPath The localization path to search for
     * @param entries          The entries to fill the template
     *
     * @return The localized string
     */
    @NotNull
    default String localizeUser(@NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries) {
        return localize(getUserLocale(), getLocalizationBundle(), localizationPath, entries);
    }
}
