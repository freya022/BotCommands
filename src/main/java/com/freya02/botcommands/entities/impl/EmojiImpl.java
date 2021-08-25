package com.freya02.botcommands.entities.impl;

import com.freya02.botcommands.entities.Emoji;

import javax.annotation.Nonnull;

public class EmojiImpl implements Emoji {
	private final String unicode;

	public EmojiImpl(String unicode) {
		this.unicode = unicode;
	}

	@Override
	public long getIdLong() {
		throw new RuntimeException("Emojis doesn't have IDs");
	}

	@Nonnull
	@Override
	public String getAsMention() {
		return unicode;
	}

	@Override
	public String getUnicode() {
		return unicode;
	}
}
