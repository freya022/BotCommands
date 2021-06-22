package com.freya02.botcommands.menu.transformer;

public class StringTransformer implements EntryTransformer<Object> {
	@Override
	public String toString(Object o) {
		return o.toString();
	}
}
