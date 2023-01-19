package com.freya02.botcommands.api.localization.context;

import com.freya02.botcommands.api.localization.Localization;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.CheckReturnValue;

/**
 * Interface helping in localizing content, supports preset localization bundles,
 * localization prefixes and context-aware localization.
 *
 * <p>This interface also includes the guild's locale by default, if the guild is a community.
 *
 * <p>While instances of this interface are primarily injected,
 * you can also construct instances of this interface with {@link #create(String, String, DiscordLocale)}.
 *
 * @see #getGuildLocale()
 *
 * @see AppLocalizationContext
 * @see #create(String, String, DiscordLocale)
 */
public interface TextLocalizationContext extends LocalizationContext {
    /**
     * Whether this localization context has a Guild locale.
     * <br>The locale can either come from the Guild or from {@link #withGuildLocale(DiscordLocale)}.
     *
     * @return {@code true} if there is a guild locale in this context
     *
     * @see #withGuildLocale(DiscordLocale)
     * @see #getGuildLocale()
     */
    boolean hasGuildLocale();

    /**
     * Returns the {@link DiscordLocale} of the guild.
     * <br>The locale can either come from the Guild or from a {@link #withGuildLocale(DiscordLocale)}.
     *
     * <p><b>Note:</b> If the context does not provide a guild locale (such as text commands) but the event comes from a {@link Guild}, then {@link DiscordLocale#ENGLISH_US} will be returned.
     *
     * @return the DiscordLocale of the guild
     *
     * @throws IllegalStateException If the event did not happen in a Guild and the guild locale was not supplied
     * @see #hasGuildLocale()
     * @see #withGuildLocale(DiscordLocale)
     */
    @NotNull
    DiscordLocale getGuildLocale();

    @NotNull
    @Override
    @CheckReturnValue
    TextLocalizationContext withBundle(@NotNull String localizationBundle);

    @NotNull
    @Override
    @CheckReturnValue
    TextLocalizationContext withPrefix(@Nullable String localizationPrefix);

    /**
     * Localizes the provided path, in the specified bundle, with the guild's locale
     * <br>This will localize to {@code en_US} if the Guild does not have the {@code COMMUNITY} feature flag
     *
     * @param localizationBundle The name of the localization bundle
     * @param localizationPath   The localization path to search for
     * @param entries            The entries to fill the template
     *
     * @throws IllegalStateException If the event did not happen in a Guild
     * @see Guild#getLocale()
     */
    @NotNull
    default String localizeGuild(@NotNull String localizationBundle, @NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries) {
        return localize(getGuildLocale(), localizationBundle, localizationPath, entries);
    }

    /**
     * Localizes the provided path, in the current context's bundle, with the guild's locale
     * <br>This will localize to {@code en_US} if the Guild does not have the {@code COMMUNITY} feature flag
     *
     * @param localizationPath The localization path to search for
     * @param entries          The entries to fill the template
     *
     * @throws IllegalStateException If the event did not happen in a Guild
     * @see Guild#getLocale()
     */
    @NotNull
    default String localizeGuild(@NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries) {
        return localize(getGuildLocale(), getLocalizationBundle(), localizationPath, entries);
    }
}
