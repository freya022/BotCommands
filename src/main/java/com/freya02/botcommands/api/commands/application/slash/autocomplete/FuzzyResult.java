package com.freya02.botcommands.api.commands.application.slash.autocomplete;

import org.jetbrains.annotations.NotNull;

public record FuzzyResult<T>(T item, String string, double distance) implements Comparable<FuzzyResult<T>> {
	public double similarity() {
		return 1d - distance;
	}

	@Override
	public int compareTo(@NotNull FuzzyResult<T> o) {
		return Double.compare(distance, o.distance);
	}
}
