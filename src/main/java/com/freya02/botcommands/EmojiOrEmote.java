package com.freya02.botcommands;

public interface EmojiOrEmote {
	boolean isEmote();

	default boolean isEmoji() {
		return !isEmote();
	}

	String getUnicode();

	String getEmoteName();

	String getId();
}
