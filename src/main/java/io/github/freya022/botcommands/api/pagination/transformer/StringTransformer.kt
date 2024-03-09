package io.github.freya022.botcommands.api.pagination.transformer;

public class StringTransformer implements EntryTransformer<Object> {
	@Override
	public String toString(Object o) {
		return o.toString();
	}
}
