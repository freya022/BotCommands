package com.freya02.botcommands.utils;

import com.vdurmont.emoji.EmojiParser;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;

public class EmojiUtils extends EmojiParser {
	/**
	 * Resolves a shortcode emoji (e.g: :joy:) into a unicode emoji for JDA to use (on reactions for example)
	 *
	 * @param input An emoji shortcode
	 * @return The unicode string of this emoji
	 */
	@NotNull
	public static String resolveEmojis(String input) {
		//Find emoji aliases (shortcodes)
		AliasCandidate alias = getAliasAt(input, 0);

		if (alias != null)
			return alias.emoji.getUnicode() + (alias.fitzpatrick == null ? "" : alias.fitzpatrick.unicode);

		//Find unicode emojis
		UnicodeCandidate candidate = getNextUnicodeCandidate(input.toCharArray(), 0);
		if (candidate != null) return candidate.getEmoji().getUnicode() + candidate.getFitzpatrickUnicode();

		throw new NoSuchElementException("No emoji for input: " + input);
	}
}
