package com.freya02.botcommands.api.entities;

public interface EmojiOrEmote {
	boolean isEmote();

	default boolean isEmoji() {
		return !isEmote();
	}

	String getUnicode();

	String getEmoteName();

	String getId();
}
