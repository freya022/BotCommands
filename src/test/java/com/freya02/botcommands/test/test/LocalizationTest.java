package com.freya02.botcommands.test.test;

import com.freya02.botcommands.api.localization.Localization;

import java.text.MessageFormat;
import java.util.Locale;

import static com.freya02.botcommands.api.localization.Localization.Entry.entry;

public class LocalizationTest {
	public static void main(String[] args) {
		final Localization localization = Localization.getInstance("Test", Locale.FRENCH);
		final Localization localization1 = Localization.getInstance("Test", Locale.ENGLISH);
		final Localization localization2 = Localization.getInstance("Test", Locale.GERMANY);

		final String localized = localization.get("commands.ban.name").localize(entry("user", "freya02"));
		final String localized2 = localization1.get("key1").localize(entry("user", "freya02"));
		final String localized3 = localization1.get("key2").localize(entry("myNumber", 2));
		final String localized4 = localization1.get("key3").localize(entry("pi", 3.141519));
		final String localized5 = localization.get("key3").localize(entry("pi", 3.141519));

		System.out.println("localized = " + localized);
		System.out.println("localized2 = " + localized2);

		final MessageFormat format = new MessageFormat("{0, choice, -1#is negative| 0#is zero or fraction | 1#is one |1.0<is 1+ |2#is two |2<is more than 2.}", Locale.FRENCH);
		System.out.println("format = " + format.format(new Object[]{2}));

		System.out.println();
	}
}
