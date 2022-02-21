package com.freya02.botcommands.internal.localization;

public class FormattableString implements LocalizableString {
	private final String formatterName;
	private final String formatter;

	public FormattableString(String formatterName, String formatter) {
		this.formatterName = formatterName;
		this.formatter = formatter;
	}

	public String getFormatterName() {
		return formatterName;
	}

	public String format(String string) {
		if (formatter == null) return string;

		return formatter.formatted(string);
	}
}
