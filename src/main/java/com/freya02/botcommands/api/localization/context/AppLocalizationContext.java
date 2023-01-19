package com.freya02.botcommands.api.localization.context;

import com.freya02.botcommands.api.localization.Localization;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.Interaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.CheckReturnValue;

/**
 * Interface helping in localizing content, supports preset localization bundles,
 * localization prefixes and context-aware localization.
 *
 * <p>This interface also includes the user's and the guild's locale by default.
 *
 * <p>While instances of this interface are primarily injected,
 * you can also construct instances of this interface with {@link #create(String, String, DiscordLocale, DiscordLocale)}.
 *
 * @see #getUserLocale()
 * @see #getGuildLocale()
 *
 * @see #create(String, String, DiscordLocale, DiscordLocale)
 */
public interface AppLocalizationContext extends TextLocalizationContext {
    /**
     * Returns the Locale of the user
     * <br>The locale can either come from the {@link Interaction} or from {@link LocalizationContext#withGuildLocale(DiscordLocale)}.
     *
     * @return The Locale of the user
     *
     * @see #withUserLocale(DiscordLocale)
     */
    @NotNull //User locale is always provided in interactions
    DiscordLocale getUserLocale();

    @NotNull
    @Override
    @CheckReturnValue
    AppLocalizationContext withGuildLocale(@Nullable DiscordLocale guildLocale);

    @NotNull
    @Override
    @CheckReturnValue
    AppLocalizationContext withBundle(@NotNull String localizationBundle);

    @NotNull
    @Override
    @CheckReturnValue
    AppLocalizationContext withPrefix(@Nullable String localizationPrefix);

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
