package com.freya02.botcommands.api.utils;

import com.vdurmont.emoji.EmojiManager;
import net.dv8tion.jda.api.entities.Emoji;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;

public class EmojiUtils {
	/**
	 * Resolves a shortcode/alias emoji (e.g: :joy:) into a unicode emoji for JDA to use (on reactions for example)
	 *
	 * @param input An emoji shortcode
	 * @return The unicode string of this emoji
	 */
	@NotNull
	public static String resolveEmojis(String input) {
		final com.vdurmont.emoji.Emoji emoji = EmojiManager.getForAlias(input);

		if (emoji == null) throw new NoSuchElementException("No emoji for input: " + input);
		return emoji.getUnicode();
	}

	public static Emoji resolveJDAEmoji(String input) {
		return Emoji.fromUnicode(resolveEmojis(input));
	}
}
