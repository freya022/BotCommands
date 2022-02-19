package com.freya02.botcommands.test.test;

import java.util.Locale;

import static com.freya02.botcommands.test.test.Localization.Entry.entry;

public class LocalizationTest {
	public static void main(String[] args) {
		final Localization localization = Localization.getInstance("Test", Locale.FRENCH);
		final Localization localization1 = Localization.getInstance("Test", Locale.ENGLISH);
		final Localization localization2 = Localization.getInstance("Test", Locale.ROOT);

		final String localized = localization.get("commands.ban.name").localize(entry("user", "freya02"));
		final String localized2 = localization1.get("key1").localize(entry("user", "freya02"));
//		final String localized3 = Localization.getInstance("Test", Locale.forLanguageTag("abc")).get("key1").localize(entry("user", "freya02"));

		System.out.println("localized = " + localized);
		System.out.println("localized2 = " + localized2);

		System.out.println();
	}
}
