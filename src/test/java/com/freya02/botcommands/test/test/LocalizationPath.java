package com.freya02.botcommands.test.test;

import java.util.ArrayList;
import java.util.List;

record LocalizationPath(List<String> paths) {
	LocalizationPath() {
		this(List.of());
	}

	public LocalizationPath resolve(String other) {
		final ArrayList<String> strings = new ArrayList<>(paths);
		strings.add(other);

		return new LocalizationPath(strings);
	}

	@Override
	public String toString() {
		return String.join(".", paths);
	}
}
