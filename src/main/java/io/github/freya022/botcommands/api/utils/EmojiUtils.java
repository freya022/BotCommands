package io.github.freya022.botcommands.api.utils;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.fellbaum.jemoji.EmojiManager;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;

public class EmojiUtils {
	/**
	 * Resolves a shortcode/alias emoji (e.g. :joy:) into an unicode emoji for JDA to use (on reactions, for example)
	 * <br>This will return itself if the input was a valid unicode emoji
	 *
	 * @param input An emoji shortcode
	 * @return The unicode string of this emoji
	 */
	@NotNull
	public static String resolveEmoji(@NotNull String input) {
		var emoji = EmojiManager.getByDiscordAlias(input);

		if (emoji.isEmpty()) emoji = EmojiManager.getEmoji(input);
		if (emoji.isEmpty()) throw new NoSuchElementException("No emoji for input: " + input);
		return emoji.get().getUnicode();
	}

	@NotNull
	public static Emoji resolveJDAEmoji(@NotNull String input) {
		return Emoji.fromUnicode(resolveEmoji(input));
	}
}
