package com.freya02.botcommands.api.localization;

import com.freya02.botcommands.internal.localization.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocalizationTemplate {
	private static final Pattern BRACKETS_PATTERN = Pattern.compile("\\{.*?}");
	private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\{(\\w+?)(?::(%.+))?}");
	private static final Pattern MESSAGE_FORMAT_PATTERN = Pattern.compile("\\{(\\w+)(,?.*?)}");

	private final List<LocalizableString> localizableStrings = new ArrayList<>();
	private final String template;

	public LocalizationTemplate(@NotNull String template, @NotNull Locale locale) {
		this.template = template;

		final Matcher bracketMatcher = BRACKETS_PATTERN.matcher(template);

		int start = 0;
		while (bracketMatcher.find()) {
			addRawString(template.substring(start, bracketMatcher.start()));

			final Matcher templateMatcher = TEMPLATE_PATTERN.matcher(bracketMatcher.group());
			if (templateMatcher.matches()) {
				final String formatterName = templateMatcher.group(1);
				final String formatter = templateMatcher.group(2);

				localizableStrings.add(new JavaFormattableString(formatterName, formatter));
			} else {
				final Matcher messageFormatMatcher = MESSAGE_FORMAT_PATTERN.matcher(bracketMatcher.group());
				if (!messageFormatMatcher.matches()) {
					throw new IllegalArgumentException("Invalid MessageFormat format '%s' in template '%s'".formatted(bracketMatcher.group(), template));
				}

				final String formatterName = messageFormatMatcher.group(1);
				final String messageFormatter = messageFormatMatcher.replaceFirst("{0$2}"); //Replace named index by integer index

				localizableStrings.add(new MessageFormatString(formatterName, messageFormatter, locale));
			}

			start = bracketMatcher.end();
		}

		addRawString(template.substring(start));
	}

	private void addRawString(String substring) {
		if (substring.isEmpty()) return;

		localizableStrings.add(new RawString(substring));
	}

	@NotNull
	public String localize(Localization.Entry... args) {
		final StringBuilder sb = new StringBuilder();

		for (LocalizableString localizableString : localizableStrings) {
			if (localizableString instanceof RawString rawString) {
				sb.append(rawString.get());
			} else if (localizableString instanceof FormattableString formattableString) {
				final Object value = getValueByFormatterName(args, formattableString.getFormatterName());

				sb.append(formattableString.format(value));
			}
		}

		return sb.toString();
	}

	private Object getValueByFormatterName(Localization.Entry[] args, String formatterName) {
		for (Localization.Entry entry : args) {
			if (entry.key().equals(formatterName)) {
				return entry.value();
			}
		}

		throw new IllegalArgumentException("Could not find format '%s' in template: '%s'".formatted(formatterName, template));
	}
}
