package com.freya02.botcommands.test.test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocalizationTemplate {
	private static final Pattern BRACKETS_PATTERN = Pattern.compile("\\{.*?}");
	private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\{(\\w+?)(?::(%.+))?}");

	private final List<LocalizableString> localizableStrings = new ArrayList<>();
	private final String template;

	public LocalizationTemplate(String template) {
		this.template = template;

		final Matcher bracketMatcher = BRACKETS_PATTERN.matcher(template);

		int start = 0;
		while (bracketMatcher.find()) {
			final Matcher templateMatcher = TEMPLATE_PATTERN.matcher(bracketMatcher.group());
			if (!templateMatcher.matches()) {
				throw new IllegalArgumentException("Invalid format '%s' in template '%s'".formatted(bracketMatcher.group(), template));
			}

			addRawString(template.substring(start, bracketMatcher.start()));

			final String formatterName = templateMatcher.group(1);
			final String formatter = templateMatcher.group(2);

			localizableStrings.add(new FormattableString(formatterName, formatter));

			start = bracketMatcher.end();
		}

		addRawString(template.substring(start));
	}

	private void addRawString(String substring) {
		if (substring.isEmpty()) return;

		localizableStrings.add(new RawString(substring));
	}

	public String localize(Localization.Entry... args) {
		final StringBuilder sb = new StringBuilder();

		for (LocalizableString localizableString : localizableStrings) {
			if (localizableString instanceof RawString rawString) {
				sb.append(rawString.get());
			} else if (localizableString instanceof FormattableString formattableString) {
				final String value = getValueByFormatterName(args, formattableString.getFormatterName());

				sb.append(formattableString.format(value));
			}
		}

		return sb.toString();
	}

	private String getValueByFormatterName(Localization.Entry[] args, String formatterName) {
		for (Localization.Entry entry : args) {
			if (entry.key().equals(formatterName)) {
				return entry.value();
			}
		}

		throw new IllegalArgumentException("Could not find format '%s' in template: '%s'".formatted(formatterName, template));
	}
}
