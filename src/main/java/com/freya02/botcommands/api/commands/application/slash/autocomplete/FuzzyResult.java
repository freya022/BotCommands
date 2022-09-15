package com.freya02.botcommands.api.commands.application.slash.autocomplete;

import org.jetbrains.annotations.NotNull;

public record FuzzyResult<T>(T item, String string, double distance) implements Comparable<FuzzyResult<T>> {
	public double similarity() {
		return 1d - distance;
	}

	@Override
	public int compareTo(@NotNull FuzzyResult<T> o) {
		if (distance == o.distance) { //This is needed as TreeSet considers entries as duplicated if compare result is 0
			return string.compareTo(o.string);
		}

		return Double.compare(distance, o.distance);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		FuzzyResult<?> that = (FuzzyResult<?>) o;

		return item.equals(that.item);
	}

	@Override
	public int hashCode() {
		return item.hashCode();
	}
}
