package com.freya02.botcommands.internal.application.slash.autocomplete;

import java.util.Arrays;

public class CompositeAutocompletionKey {
	private final String[] keys;
	private final int length;
	private final int hashCode;

	public CompositeAutocompletionKey(String[] keys) {
		this.keys = keys;

		int length = 0;
		for (String key : keys) {
			length += key.length();
		}

		this.length = length;

		this.hashCode = Arrays.hashCode(this.keys);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CompositeAutocompletionKey that = (CompositeAutocompletionKey) o;

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
