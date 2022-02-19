package com.freya02.botcommands.test.test;

public class RawString implements LocalizableString {
	private final String string;

	public RawString(String string) {
		this.string = string;
	}

	public String get() {
		return string;
	}
}
