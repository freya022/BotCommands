package com.freya02.botcommands.utils;

import com.vdurmont.emoji.EmojiParser;

public class EmojiResolver extends EmojiParser {
	public static String resolveEmojis(String input) {
		//Find emoji aliases (shortcodes)
		AliasCandidate alias = getAliasAt(input, 0);

		if (alias != null)
			return alias.emoji.getUnicode() + (alias.fitzpatrick == null ? "" : alias.fitzpatrick.unicode);

		//Find unicode emojis
		UnicodeCandidate candidate = getNextUnicodeCandidate(input.toCharArray(), 0);
		if (candidate != null) return candidate.getEmoji().getUnicode() + candidate.getFitzpatrickUnicode();

		return null;
	}
}
