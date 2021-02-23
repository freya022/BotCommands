package com.freya02.botcommands;

import org.jetbrains.annotations.NotNull;

class EmojiImpl implements Emoji {
	private final String substring;

	public EmojiImpl(String substring) {
		this.substring = substring;
	}

	@Override
	public long getIdLong() {
		throw new RuntimeException("Emojis doesn't have IDs");
	}

	@NotNull
	@Override
	public String getAsMention() {
		return substring;
	}

	@Override
	public String getUnicode() {
		return substring;
	}
}
