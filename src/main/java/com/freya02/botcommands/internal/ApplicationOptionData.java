package com.freya02.botcommands.internal;

import com.freya02.botcommands.api.application.annotations.Option;

import java.lang.reflect.Parameter;

public class ApplicationOptionData {
	private final String effectiveName, effectiveDescription;

	public ApplicationOptionData(Parameter parameter) {

		
		final Option option = parameter.getAnnotation(Option.class);

		if (option.name().isBlank()) {
			effectiveName = getOptionName(parameter);
		} else {
			effectiveName = option.name();
		}
		
		if (option.description().isBlank()) {
			effectiveDescription = "No description";
		} else {
			effectiveDescription = option.description();
		}
	}

	private static String getOptionName(Parameter parameter) {
		if (!parameter.isNamePresent())
			throw new RuntimeException("Parameter name cannot be deduced as the option's name is not specified on: " + parameter);

		final String name = parameter.getName();
		final int nameLength = name.length();

		final StringBuilder optionNameBuilder = new StringBuilder(nameLength + 10); //I doubt you'd have a parameter long enough to have more than 10 underscores
		for (int i = 0; i < nameLength; i++) {
			final char c = name.charAt(i);

			if (Character.isUpperCase(c)) {
				optionNameBuilder.append('_').append(Character.toLowerCase(c));
			} else {
				optionNameBuilder.append(c);
			}
		}

		return optionNameBuilder.toString();
	}

	/**
	 * Not localized
	 */
	public String getEffectiveName() {
		return effectiveName;
	}

	/**
	 * Not localized
	 */
	public String getEffectiveDescription() {
		return effectiveDescription;
	}
}
