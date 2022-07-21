package com.freya02.botcommands.internal.localization;

public interface FormattableString extends LocalizableString {
	String getFormatterName();

	String format(Object obj);
}
