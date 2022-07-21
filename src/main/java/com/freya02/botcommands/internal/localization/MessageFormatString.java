package com.freya02.botcommands.internal.localization;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.Locale;

public class MessageFormatString implements FormattableString {
	private final String formatterName;
	private final MessageFormat formatter;

	public MessageFormatString(@NotNull String formatterName, @Nullable String formatter, @NotNull Locale locale) {
		this.formatterName = formatterName;
		this.formatter = formatter == null ? null : new MessageFormat(formatter, locale);
	}

	@Override
	public String getFormatterName() {
		return formatterName;
	}

	@Override
	public String format(Object obj) {
		if (formatter == null) return obj.toString();

		synchronized (formatter) {
			return formatter.format(new Object[]{obj});
		}
	}
}
