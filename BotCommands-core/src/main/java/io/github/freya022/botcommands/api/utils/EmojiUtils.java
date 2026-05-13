package io.github.freya022.botcommands.api.utils;

import dev.freya02.jda.emojis.unicode.Emojis;
import dev.freya02.jda.emojis.unicode.UnicodeEmojis;
import dev.freya02.jda.emojis.unicode.UnicodeEmojisManager;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.NoSuchElementException;

/**
 * Utility class to resolve alias emojis into Unicode, and getting an {@link UnicodeEmoji} out of them.
 */
@NullMarked
public class EmojiUtils {
    /**
     * Returns the Unicode emoji from a Discord alias (e.g. {@code :joy:}).
     *
     * <p><b>Note:</b> The input string is case-sensitive!
     *
     * <p>This will return itself if the input is a valid Unicode emoji.
     *
     * @param input An emoji alias or Unicode
     *
     * @return The Unicode string of this emoji
     *
     * @throws NoSuchElementException if no emoji alias or Unicode matches
     * @see #resolveJDAEmoji(String)
     */
    public static String resolveEmoji(String input) {
        final var emoji = resolveEmojiOrNull(input);
        if (emoji == null) throw new NoSuchElementException("No emoji for input: " + input);
        return emoji;
    }

    /**
     * Returns the Unicode emoji from a Discord alias (e.g. {@code :joy:}), or {@code null} if unresolvable.
     *
     * <p><b>Note:</b> The input string is case-sensitive!
     *
     * <p>This will return itself if the input is a valid Unicode emoji.
     *
     * @param input An emoji alias or Unicode
     *
     * @return The Unicode string of this emoji, {@code null} if unresolvable
     *
     * @see #resolveJDAEmojiOrNull(String)
     */
    @Nullable
    public static String resolveEmojiOrNull(String input) {
        String emoji = UnicodeEmojisManager.getEmojiByAlias(input);
        if (emoji != null) {
            return emoji;
        }

        // In case it was a Unicode emoji already, just check it is valid
        if (UnicodeEmojisManager.isValidEmoji(input)) {
            return input;
        }

        return null;
    }

    /**
     * Returns the {@link UnicodeEmoji} from a Discord alias (e.g. {@code :joy:}).
     *
     * <p><b>Note:</b> The input string is case-sensitive!
     *
     * <p>This will return itself if the input is a valid Unicode emoji.
     *
     * @param input An emoji alias or Unicode
     *
     * @return The {@link UnicodeEmoji} of this emoji
     *
     * @throws NoSuchElementException if no emoji alias or Unicode matches
     * @see #resolveEmoji(String)
     */
    public static UnicodeEmoji resolveJDAEmoji(String input) {
        return Emoji.fromUnicode(resolveEmoji(input));
    }

    /**
     * Returns the {@link UnicodeEmoji} from a Discord alias (e.g. {@code :joy:}), or {@code null} if unresolvable.
     *
     * <p><b>Note:</b> The input string is case-sensitive!
     *
     * <p>This will return itself if the input is a valid Unicode emoji.
     *
     * @param input An emoji alias or Unicode
     *
     * @return The {@link UnicodeEmoji} of this emoji
     *
     * @see #resolveEmoji(String)
     */
    @Nullable
    public static UnicodeEmoji resolveJDAEmojiOrNull(String input) {
        final String unicode = resolveEmojiOrNull(input);
        if (unicode == null) return null;
        return Emoji.fromUnicode(unicode);
    }
}
