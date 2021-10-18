package com.freya02.botcommands.internal.entities;

import com.freya02.botcommands.api.entities.EmojiOrEmote;

public class EmojiOrEmoteImpl implements EmojiOrEmote {
	private final String unicode;
	private final String emoteName;
	private final String id;

	public EmojiOrEmoteImpl(String unicode) {
		this.unicode = unicode;
		this.emoteName = null;
		this.id = null;
	}

	public EmojiOrEmoteImpl(String emoteName, String id) {
		this.emoteName = emoteName;
		this.id = id;
		this.unicode = null;
	}

	@Override
	public boolean isEmote() {
		return unicode == null;
	}

	@Override
	public String getUnicode() {
		if (isEmote()) throw new IllegalStateException("This is not an emoji !");
		return unicode;
	}

	@Override
	public String getEmoteName() {
		if (!isEmote()) throw new IllegalStateException("This is not an emote !");
		return emoteName;
	}

	@Override
	public String getId() {
		if (!isEmote()) throw new IllegalStateException("This is not an emote !");
		return id;
	}
}
