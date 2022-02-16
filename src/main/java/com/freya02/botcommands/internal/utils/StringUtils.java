package com.freya02.botcommands.internal.utils;

import java.util.List;
import java.util.StringJoiner;

public class StringUtils {
	public static String naturalJoin(String lastElemSuffix, List<String> strings) {
		if (strings.size() == 1) return strings.get(0);

		StringBuilder sb = new StringBuilder();

		final StringJoiner joiner = new StringJoiner(", ", "", " " + lastElemSuffix);

		for (int i = 0; i < strings.size() - 1; i++) {
			joiner.add(strings.get(i));
		}

		sb.append(joiner).append(' ').append(strings.get(strings.size() - 1));

		return sb.toString();
	}
}
