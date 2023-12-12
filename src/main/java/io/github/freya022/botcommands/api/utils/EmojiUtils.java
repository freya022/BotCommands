package io.github.freya022.botcommands.api.utils;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.fellbaum.jemoji.EmojiManager;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;

public class EmojiUtils {
    private static final int REGIONAL_INDICATOR_A_CODEPOINT = 127462;
    private static final int REGIONAL_INDICATOR_Z_CODEPOINT = 127487;

    /**
     * Resolves a shortcode/alias emoji (e.g. :joy:) into an unicode emoji for JDA to use (on reactions, for example)
     * <br>This will return itself if the input was a valid unicode emoji
     *
     * @param input An emoji shortcode
     *
     * @return The unicode string of this emoji
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

    @NotNull
    public static Emoji resolveJDAEmoji(@NotNull String input) {
        return Emoji.fromUnicode(resolveEmoji(input));
    }
}
