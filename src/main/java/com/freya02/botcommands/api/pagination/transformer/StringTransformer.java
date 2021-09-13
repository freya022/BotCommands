package com.freya02.botcommands.api.pagination.transformer;

public class StringTransformer implements EntryTransformer<Object> {
	@Override
	public String toString(Object o) {
		return o.toString();
	}
}
