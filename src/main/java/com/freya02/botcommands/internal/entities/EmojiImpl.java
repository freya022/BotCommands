package com.freya02.botcommands.internal.entities;

import com.freya02.botcommands.api.entities.Emoji;
import org.jetbrains.annotations.NotNull;

public class EmojiImpl implements Emoji {
	private final String unicode;

	public EmojiImpl(String unicode) {
		this.unicode = unicode;
	}

	@Override
	public long getIdLong() {
		throw new RuntimeException("Emojis doesn't have IDs");
	}

	@NotNull
	@Override
	public String getAsMention() {
		return unicode;
	}

	@Override
	public String getUnicode() {
		return unicode;
	}
}
