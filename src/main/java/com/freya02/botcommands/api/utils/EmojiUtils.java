package com.freya02.botcommands.api.utils;

import com.freya02.emojis.Emojis;
import net.dv8tion.jda.api.entities.Emoji;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;

public class EmojiUtils {
	/**
	 * Resolves a shortcode emoji (e.g: :joy:) into a unicode emoji for JDA to use (on reactions for example)
	 *
	 * @param input An emoji shortcode
	 * @return The unicode string of this emoji
	 */
	@NotNull
	public static String resolveEmojis(String input) {
		final com.freya02.emojis.Emoji emoji = Emojis.ofShortcode(input);

		if (emoji == null) throw new NoSuchElementException("No emoji for input: " + input);
		return emoji.unicode();
	}

	public static Emoji resolveJdaEmoji(String input) {
		return Emoji.fromUnicode(resolveEmojis(input));
	}
}
