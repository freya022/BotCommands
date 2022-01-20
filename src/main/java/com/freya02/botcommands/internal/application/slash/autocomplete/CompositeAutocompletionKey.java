package com.freya02.botcommands.internal.application.slash.autocomplete;

import java.util.Arrays;

public class CompositeAutocompletionKey {
	private final String[] keys;
	private final long guildId;
	private final long channelId;
	private final long userId;

	private final int length;
	private final int hashCode;

	public CompositeAutocompletionKey(String[] keys, long guildId, long channelId, long userId) {
		this.keys = keys;

		this.guildId = guildId;
		this.channelId = channelId;
		this.userId = userId;

		int length = 0;
		for (String key : keys) {
			length += key.length();
		}

		this.length = length;

		int hashCode = Arrays.hashCode(keys);
		hashCode = 31 * hashCode + (int) (guildId ^ (guildId >>> 32));
		hashCode = 31 * hashCode + (int) (channelId ^ (channelId >>> 32));
		hashCode = 31 * hashCode + (int) (userId ^ (userId >>> 32));

		this.hashCode = hashCode;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CompositeAutocompletionKey that = (CompositeAutocompletionKey) o;

		if (guildId != that.guildId) return false;
		if (channelId != that.channelId) return false;
		if (userId != that.userId) return false;
		// Probably incorrect - comparing Object[] arrays with Arrays.equals
		return Arrays.equals(keys, that.keys);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	public int length() {
		return length;
	}
}
