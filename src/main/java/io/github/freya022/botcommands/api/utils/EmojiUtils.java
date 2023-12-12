package io.github.freya022.botcommands.api.utils;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;
import net.fellbaum.jemoji.EmojiManager;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;

/**
 * Utility class to resolve alias emojis into unicode, and getting an {@link UnicodeEmoji} out of them.
 */
public class EmojiUtils {
    private static final int REGIONAL_INDICATOR_A_CODEPOINT = 127462;
    private static final int REGIONAL_INDICATOR_Z_CODEPOINT = 127487;

    /**
     * Resolves a shortcode/alias emoji (e.g. {@code :joy:}) into an unicode emoji,
     * for JDA to use (on reactions, for example).
     *
     * <p><b>Note:</b> The input string is case-sensitive!
     *
     * <p>This will return itself if the input is a valid unicode emoji.
     *
     * @param input An emoji alias or unicode
     *
     * @return The unicode string of this emoji
     *
     * @throws NoSuchElementException if no emoji alias or unicode matches
     *
     * @see #resolveJDAEmoji(String)
     */
    @NotNull
    public static String resolveEmoji(@NotNull String input) {
        var emoji = EmojiManager.getByDiscordAlias(input);

        if (emoji.isEmpty()) emoji = EmojiManager.getEmoji(input);
        if (emoji.isEmpty()) {
            // Try to get regional indicators https://github.com/felldo/JEmoji/issues/44
            final var alias = removeColonFromAlias(input);
            if (alias.startsWith("regional_indicator_")) {
                final char character = alias.charAt(19);
                if (character >= 'a' && character <= 'z') {
                    final int codepoint = REGIONAL_INDICATOR_A_CODEPOINT + (character - 'a');
                    return Character.toString(codepoint);
                }
            } else {
                final int codepoint = input.codePointAt(0);
                if (codepoint >= REGIONAL_INDICATOR_A_CODEPOINT && codepoint <= REGIONAL_INDICATOR_Z_CODEPOINT) {
                    return input;
                }
            }
            throw new NoSuchElementException("No emoji for input: " + input);
        }
        return emoji.get().getUnicode();
    }

    @NotNull
    private static String removeColonFromAlias(@NotNull final String alias) {
        return alias.startsWith(":") && alias.endsWith(":") ? alias.substring(1, alias.length() - 1) : alias;
    }

    /**
     * Resolves a shortcode/alias emoji (e.g. {@code :joy:}) into an {@link UnicodeEmoji}.
     *
     * <p><b>Note:</b> The input string is case-sensitive!
     *
     * <p>This will return itself if the input is a valid unicode emoji.
     *
     * @param input An emoji alias or unicode
     *
     * @return The {@link UnicodeEmoji} of this emoji
     *
     * @throws NoSuchElementException if no emoji alias or unicode matches
     *
     * @see #resolveEmoji(String)
     */
    @NotNull
    public static UnicodeEmoji resolveJDAEmoji(@NotNull String input) {
        return Emoji.fromUnicode(resolveEmoji(input));
    }
}
