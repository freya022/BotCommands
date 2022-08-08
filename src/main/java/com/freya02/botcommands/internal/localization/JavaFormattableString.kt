package com.freya02.botcommands.internal.localization;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JavaFormattableString implements FormattableString {
	private final String formatterName;
	private final String formatter;

	public JavaFormattableString(@NotNull String formatterName, @Nullable String formatter) {
		this.formatterName = formatterName;
		this.formatter = formatter;
	}

	@Override
	public String getFormatterName() {
		return formatterName;
	}

	@Override
	public String format(Object obj) {
		if (formatter == null) return obj.toString();

		return formatter.formatted(obj);
	}
}
