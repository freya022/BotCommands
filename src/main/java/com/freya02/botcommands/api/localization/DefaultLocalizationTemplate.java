package com.freya02.botcommands.api.localization;

import com.freya02.botcommands.internal.localization.*;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Default implementation for {@link LocalizationTemplate}
 * <p><b>Specification:</b>
 * <br>In a nutshell, this is a copy of the {@link MessageFormat} specification, but with named parameters
 * <br>To declare a variable inside your localization template, you may use "<code>{variable_name}</code>"
 * <br>As this supports {@link MessageFormat}, you can also specify format types, such as: "<code>{variable_name, number}</code>",
 * and format styles, such as: "<code>{@literal {user_amount, choice, 0#users|1#user|1<users}}</code>"
 *
 * <p>Full example: <code>{@literal "There are {user_amount} {user_amount, choice, 0#users|1#user|1<users} and my up-time is {uptime, number} seconds"}</code>
 */
public class DefaultLocalizationTemplate implements LocalizationTemplate {
	private static final Pattern BRACKETS_PATTERN = Pattern.compile("\\{.*?}");
	private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\{(\\w+?)(?::(%.+))?}");
	private static final Pattern MESSAGE_FORMAT_PATTERN = Pattern.compile("\\{(\\w+)(,?.*?)}");

	private final List<LocalizableString> localizableStrings = new ArrayList<>();
	private final String template;

	public DefaultLocalizationTemplate(@NotNull String template, @NotNull Locale locale) {
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

	@Override
	@NotNull
	public String localize(Localization.Entry... args) {
		final StringBuilder sb = new StringBuilder();

		for (LocalizableString localizableString : localizableStrings) {
			if (localizableString instanceof RawString rawString) {
				sb.append(rawString.get());
			} else if (localizableString instanceof FormattableString formattableString) {
				final Object value = getValueByFormatterName(args, formattableString.getFormatterName());
				try {
					sb.append(formattableString.format(value));
				} catch (Exception e) { //For example, if the user provided a string to a number format
					throw new RuntimeException("Could not get localized string from FormattableString '%s' with value '%s'".formatted(formattableString.getFormatterName(), value), e);
				}
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

	@Override
	public String toString() {
		return template;
	}
}
