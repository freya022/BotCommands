package com.freya02.botcommands.pagination.transformer;

public class StringTransformer implements EntryTransformer<Object> {
	@Override
	public String toString(Object o) {
		return o.toString();
	}
}
